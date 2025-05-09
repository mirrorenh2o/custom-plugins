package org.ruyisdk.ruyi.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Scanner;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.ruyisdk.core.config.Constants;
import org.ruyisdk.core.ruyi.model.RepoConfig;
import org.ruyisdk.core.ruyi.model.RuyiReleaseInfo;
import org.ruyisdk.core.ruyi.model.SystemInfo;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.preferences.RuyiPreferenceConstants;
import org.ruyisdk.ruyi.services.RuyiManager;
import org.ruyisdk.ruyi.ui.dialogs.RuyiInstallWizard.InstallationListener;
import org.ruyisdk.ruyi.util.PathUtils;
import org.ruyisdk.ruyi.util.RuyiFileUtils;
import org.ruyisdk.ruyi.util.RuyiLogger;
import org.ruyisdk.ruyi.util.RuyiNetworkUtils;

public class RuyiInstallManager {
	private final RuyiLogger logger;

	private static final Path DEFAULT_INSTALL_PATH = RuyiFileUtils.getDefaultInstallPath(); // 经过处理的绝对路径path
	// System.getProperty("user.home") + File.separator + ".ruyi";
	private String installPath; // UI界面用户自定义的path

	// ruyi 下载相关参数
	private String installedVersion; // 本地已安装ruyi版本
	private RuyiReleaseInfo latestRelease; // 接口获取的 ruyi Release信息，含版本和下载url
	private String latestVersion;

	// ruyi 配置相关参数
	private RepoConfig[] repoUrls; // 存储库地址，支持多个
	private String repositoryUrl;
	private boolean telemetryEnabled;

	public RuyiInstallManager(RuyiLogger logger) {
		this.logger = logger;
		this.installPath = DEFAULT_INSTALL_PATH.toString();
//        this.installedVersion = 
//        this.latestRelease = 
		this.repositoryUrl = "";
		this.telemetryEnabled = true;
	}

	// ========== 安装管理方法 ==========

	public void install(IProgressMonitor monitor, InstallationListener listener) throws Exception {
		SubMonitor subMonitor = SubMonitor.convert(monitor, "Installing Ruyi", 100);

		try {
			// 阶段1: 准备安装 (10%)
			subMonitor.subTask("Preparing installation");
			prepareInstallation(listener);
			subMonitor.worked(10);

			// 清理/备份旧文件

			// 阶段2: 下载Ruyi (50%)
			subMonitor.subTask("Downloading components");
			downloadRuyi(subMonitor.newChild(60), listener);

//            // 阶段3: 安装文件 (30%)
//            subMonitor.subTask("Installing files");
//            installFiles(subMonitor.newChild(30), listener);

			// 阶段3: 完成安装 (10%)
			subMonitor.subTask("Finalizing installation");
			listener.logMessage("Setting up environment...");
//         // 3. 文件预处理
//            prepareExecutable(Paths.get(installPath, latestRelease.getFilename()), 
//            		Paths.get(installPath,  "ruyi"),
//            		listener);

			// 4. 环境变量配置
//            addToPathIfNeeded(installPath, listener);
			addToPathIfNeeded(DEFAULT_INSTALL_PATH.toString(), listener);
			subMonitor.worked(90);
			listener.logMessage("Ruyi " + getVersionFromPackage() + " installed successfully");

			// 阶段3: 验证安装 (10%)
			subMonitor.subTask("Validating installation");
			validateInstallation(installPath, listener);
			subMonitor.worked(100);

			listener.logMessage("Installation completed successfully");
		} catch (Exception e) {
			listener.logMessage("Installation failed: " + e.getMessage());
			throw e;
		} finally {
			subMonitor.done();
		}
	}

	private void prepareInstallation(InstallationListener listener) throws Exception {
		listener.logMessage("Verifying installation directory: " + installPath);
		Path path = Paths.get(installPath);
		System.out.println("Ruyi包管理器安装地址：" + path);

		// 1. 确保目录存在（不存在则创建）
		if (!Files.exists(path)) {
			listener.logMessage("Directory does not exist, attempting to create...");
			try {
				Files.createDirectories(path); // 递归创建所有缺失的父目录
				listener.logMessage("Directory created successfully.");
			} catch (IOException e) {
				throw new Exception("Failed to create installation directory: " + e.getMessage(), e);
			}
		} else {
			listener.logMessage("Directory already exists.");
		}
		listener.progressChanged(5, "Directory ready");

//        // 2. 检查磁盘剩余空间（假设至少需要 500MB 空间）
//        listener.logMessage("Checking available disk space...");
//        long requiredSpaceBytes = 500L * 1024 * 1024;  // 500M
//        File diskPartition = new File(installPath);
//        long freeSpaceBytes = diskPartition.getUsableSpace();
//
//        if (freeSpaceBytes < requiredSpaceBytes) {
//            throw new Exception(String.format(
//                "Insufficient disk space. Required: %.1f MB, Available: %.1f MB",
//                requiredSpaceBytes / (1024L * 1024),
//                freeSpaceBytes / (1024L * 1024)
//            ));
//        }
//        listener.logMessage(String.format(
//            "Disk space OK (Available: %.1f MB)", 
//            freeSpaceBytes / (1024L * 1024)
//        ));
////        listener.logMessage("Disk space OK");
//        listener.progressChanged(10, "Preparation complete");

		// 2. 检查磁盘空间（使用 NIO API）
		listener.logMessage("Checking available disk space...");
		FileStore store;
		try {
			store = Files.getFileStore(path);
		} catch (IOException e) {
			throw new Exception("Cannot access filesystem: " + e.getMessage(), e);
		}

		long freeSpaceBytes = store.getUsableSpace();
		long requiredSpaceBytes = 500L * 1024 * 1024;

		// 数值比较
		if (freeSpaceBytes < requiredSpaceBytes) {
			BigDecimal requiredMB = BigDecimal.valueOf(requiredSpaceBytes).divide(BigDecimal.valueOf(1024L * 1024L), 1,
					RoundingMode.HALF_UP);
			BigDecimal freeMB = BigDecimal.valueOf(freeSpaceBytes).divide(BigDecimal.valueOf(1024L * 1024L), 1,
					RoundingMode.HALF_UP);

			throw new Exception(String.format("需要至少 %s MB 空间，当前可用 %s MB",
					requiredMB.stripTrailingZeros().toPlainString(), freeMB.stripTrailingZeros().toPlainString()));
		}

		// 构造安全日志消息
		BigDecimal freeMB = BigDecimal.valueOf(freeSpaceBytes).divide(BigDecimal.valueOf(1024L * 1024L), 1,
				RoundingMode.HALF_UP);
		String msg = "磁盘空间充足 (可用: " + freeMB.toPlainString() + " MB)";
		listener.logMessage(msg);
		listener.progressChanged(10, "准备完成");
	}

	private void downloadRuyi(IProgressMonitor monitor, InstallationListener listener) throws Exception {
		String archSuffix = SystemInfo.detectArchitecture().getSuffix();
		latestRelease = RuyiAPI.getLatestRelease(archSuffix);
//        String ruyiInstallPath = Paths.get(installPath, latestRelease.getFilename()).toString();
		Path ruyiInstallPath = Paths.get(RuyiFileUtils.getDefaultInstallPath().toString(), "ruyi");
		Path parent = ruyiInstallPath.getParent();
		if (!Files.exists(parent)) {
			Files.createDirectories(parent);
		}

		// 定义下载源数组（按优先级排序）
		String[] downloadSources = { latestRelease.getMirrorUrl(), // 优先尝试镜像地址
				latestRelease.getGithubUrl() // 镜像失败后尝试GitHub
		};

		Exception lastException = null;

		for (int i = 0; i < downloadSources.length; i++) {
			String sourceName = i == 0 ? "镜像源" : "GitHub源";
			String ruyiDownloadUrl = downloadSources[i];
			System.out.println("ruyiDownloadUrl===" + ruyiDownloadUrl);
			System.out.println("ruyiInstallPath===" + ruyiInstallPath);

			try {
				listener.logMessage(String.format("尝试从%s下载: %s", sourceName, ruyiDownloadUrl));

				RuyiNetworkUtils.downloadFile(ruyiDownloadUrl, ruyiInstallPath.toString(), monitor,
						(transferred, total) -> {
							int percent = (int) ((double) transferred / total * 100);
							listener.progressChanged(10 + percent / 2,
									String.format("从%s下载中 (%d/%d KB)", sourceName, transferred / 1024, total / 1024));
						});
				listener.logMessage("下载成功");
				return; // 下载成功则直接返回

			} catch (Exception e) {
				lastException = e;
				listener.logMessage(String.format("%s下载失败: %s", sourceName, e.getMessage()));

				// 如果是最后一次尝试，不再删除文件（保留可能的部分下载）
				if (i < downloadSources.length - 1) {
					try {
						Files.deleteIfExists(ruyiInstallPath);
						listener.logMessage("已清理失败下载的部分文件");
					} catch (IOException ioEx) {
						listener.logMessage("清理部分文件失败: " + ioEx.getMessage());
					}
				}
			}
		}

		// 所有下载源均失败
		throw new Exception(
				String.format("所有下载尝试均失败。最后错误: %s", lastException != null ? lastException.getMessage() : "未知错误"),
				lastException);
	}

	private void downloadRuyi1(IProgressMonitor monitor, InstallationListener listener) throws Exception {
		String lastestVersion = "0.32.0";
		String archSuffix = SystemInfo.detectArchitecture().getSuffix(); // "amd64"
		String lastestFileName = "ruyi." + archSuffix;
		String ruyiInstallPath = Paths.get(installPath, lastestFileName).toString();

		Exception lastException = null; // 记录最后一次失败的原因

		// 遍历所有 RepoConfig，按优先级顺序尝试下载
		for (RepoConfig repoConfig : repoUrls) {
			try {
				String ruyiDownloadUrl = repoConfig.getUrl() + "/" + lastestVersion + "/" + lastestFileName;
				listener.logMessage("Trying download from: " + ruyiDownloadUrl);

				RuyiNetworkUtils.downloadFile(ruyiDownloadUrl, ruyiInstallPath, monitor, (transferred, total) -> {
					int percent = (int) ((double) transferred / total * 100);
					listener.progressChanged(10 + percent / 2, String.format("Downloading from %s (%d/%d KB)",
							repoConfig.getName(), transferred / 1024, total / 1024));
				});

				// 下载成功，直接返回
				listener.logMessage("Download succeeded from: " + repoConfig.getName());
				return;

			} catch (Exception e) {
				// 记录失败原因，继续尝试下一个地址
				lastException = e;
				listener.logMessage("Download failed from " + repoConfig.getName() + ": " + e.getMessage());
			}
		}

		// 所有地址均失败，抛出最后一次的异常
		if (lastException != null) {
			throw new Exception("All download attempts failed. Last error: " + lastException.getMessage(),
					lastException);
		} else {
			throw new Exception("No valid download URLs configured.");
		}
	}

	private void finalizeInstallation(InstallationListener listener) throws Exception {
		listener.logMessage("Setting up environment...");
//        // 环境配置逻辑...
//        for (int i = 0; i < 10; i++) {
//            if (monitor.isCanceled()) {
//                throw new InterruptedException("Installation cancelled by user");
//            }
//            Thread.sleep(100);
//            monitor.worked(1);
//            listener.progressChanged(90 + i, "Finalizing...");
//        }
//        
		this.installedVersion = getVersionFromPackage();
		listener.logMessage("Ruyi " + installedVersion + " installed successfully");
	}

	private void prepareExecutable(Path source, Path target, InstallationListener listener) throws Exception {
		// 1. 复制/重命名文件
		listener.logMessage("Preparing executable...");
//        Files.deleteIfExists(target); // 删除已存在的旧文件
//        Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);

		// 2. 设置可执行权限 (Unix-like系统)
		try {
			if (!target.toFile().setExecutable(true)) {
				throw new Exception("Failed to set executable permission");
			}
		} catch (SecurityException e) {
			throw new Exception("Permission denied when setting executable flag", e);
		}

		listener.logMessage("Executable prepared: " + target);
	}

	private void addToPathIfNeeded(String path, InstallationListener listener) throws Exception {
		Path ruyiExecutable = Paths.get(path, "ruyi");

		// 1. 检查是否已配置
		if (PathUtils.isPathConfigured(path) && Files.isExecutable(ruyiExecutable)) {
			listener.logMessage("PATH already contains: " + path);
			return;
		}

		// 2. 弹出图形化确认
		if (!showConfirmationDialog("PATH Configuration", "需要添加安装目录到PATH变量并设置可执行权限")) {
			throw new Exception("User declined configuration");
		}

		// 3. 准备配置命令（包含PATH设置和权限设置）
		String command = String.format("echo 'export PATH=\"%s:$PATH\"' >> ~/.bashrc && " + // 添加PATH
				"chmod +x %s && " + // 设置可执行权限
				"source ~/.bashrc", // 立即生效
				path, ruyiExecutable.toString());

		// 4. 执行特权命令
		executeWithPrivilege(command, listener);

		// 5. 验证权限
		if (!Files.isExecutable(ruyiExecutable)) {
			// 详细错误诊断
			String perms = Files.exists(ruyiExecutable)
					? "Current permissions: " + Files.getPosixFilePermissions(ruyiExecutable)
					: "File does not exist";
			throw new Exception(String.format("Permission verification failed for: %s\n%s", ruyiExecutable, perms));
		}
//        listener.logMessage("Successfully set executable permission for: " + ruyiExecutable);
		listener.logMessage("验证成功: " + ruyiExecutable + " 已获得可执行权限");
	}

	private boolean showConfirmationDialog(String title, String message) {
		try {
			// 尝试使用zenity图形对话框
			return new ProcessBuilder("zenity", "--question", "--title=" + title, "--text=" + message, "--width=300")
					.start().waitFor() == 0;
		} catch (Exception e) {
			// 回退到控制台确认
			System.out.printf("%s (y/N): ", message);
			return new Scanner(System.in).nextLine().equalsIgnoreCase("y");
		}
	}

	private void executeWithPrivilege(String command, InstallationListener listener) throws Exception {
//        String[] cmd = {
//            "pkexec",
//            "bash",
//            "-c",
//            String.format("su $SUDO_USER -c '%s'", command)
//        };

		// 先扩展所有~符号
		String expandedCmd = command.replace("~", System.getProperty("user.home"));

		String[] cmd = { "pkexec", "bash", "-c",
				// 直接执行命令（不再通过su，因为pkexec已经提权）
				expandedCmd };

		try {
			Process proc = new ProcessBuilder(cmd).redirectErrorStream(true).start();

			// 读取输出
			StringBuilder output = new StringBuilder();
			try (BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()))) {
				String line;
				while ((line = reader.readLine()) != null) {
					output.append(line).append("\n");
					listener.logMessage(line);
				}
			}

			int exitCode = proc.waitFor();
			if (exitCode != 0) {
				throw new Exception(String.format("Command failed (exit code %d): %s\nOutput: %s", exitCode, command,
						output.toString()));
			}
		} catch (IOException | InterruptedException e) {
			throw new Exception("Failed to execute privileged command: " + e.getMessage(), e);
		}
	}

	private void validateInstallation(String ruyiPath, InstallationListener listener) throws Exception {
		listener.logMessage("Validating installation...");

		// 执行 ruyi -V 命令验证
		ProcessBuilder pb = new ProcessBuilder(ruyiPath + "/ruyi", "-V");
		pb.redirectErrorStream(true);

		try {
			Process process = pb.start();
			int exitCode = process.waitFor();

			if (exitCode != 0) {
				String output = new String(process.getInputStream().readAllBytes());
				throw new Exception("Validation failed. Output:\n" + output);
			}
		} catch (IOException | InterruptedException e) {
			throw new Exception("Failed to validate installation", e);
		}

		listener.logMessage("Validation successful");
	}

	private void cleanFailedDownload(Path file, InstallationListener listener) {
		try {
			Files.deleteIfExists(file);
		} catch (IOException e) {
			listener.logMessage("Warning: Failed to clean up temporary file - " + e.getMessage());
		}
	}

	private void updateProgress(long transferred, long total, InstallationListener listener) {
		int percent = (int) ((double) transferred / total * 100);
		listener.progressChanged(10 + percent / 2,
				String.format("Downloading (%d/%d KB)", transferred / 1024, total / 1024));
	}

	// ========== 版本管理方法 ==========

	public boolean isInstalled() throws Exception {
		Path ruyiBin = Paths.get(installPath, "bin", "ruyi");
		return RuyiFileUtils.isExecutable(ruyiBin.toString());
	}

	public String getInstalledVersion() throws Exception {
		if (installedVersion != null) {
			return installedVersion;
		}

		if (!isInstalled()) {
			return null;
		}

		Path versionFile = Paths.get(installPath, "VERSION");
		if (Files.exists(versionFile)) {
			installedVersion = RuyiFileUtils.readFileContent(versionFile.toString()).trim();
			return installedVersion;
		}
		return "unknown";
	}

	public String getLatestVersion() throws Exception {
		if (latestVersion != null) {
			return latestVersion;
		}

		String versionUrl = repositoryUrl + "/version/latest";
		latestVersion = RuyiNetworkUtils.fetchStringContent(versionUrl, null);
		return latestVersion;
	}

	public String getChangelog(String version) throws Exception {
		String changelogUrl = repositoryUrl + "/changelog/" + version;
		return RuyiNetworkUtils.fetchStringContent(changelogUrl, null);
	}

	private String getVersionFromPackage() throws Exception {
		// 从安装包中提取版本信息
		Path versionFile = Paths.get(installPath, "VERSION");
		if (Files.exists(versionFile)) {
			return RuyiFileUtils.readFileContent(versionFile.toString()).trim();
		}
		return "unknown";
	}

	// ========== 配置管理方法 ==========

	public String getDefaultInstallPath() {
		return DEFAULT_INSTALL_PATH.toString();
	}

	public void setInstallPath(String installPath) {
		this.installPath = installPath;
	}

	public String getInstallPath() {
		return installPath;
	}

	public void setRepoUrls(RepoConfig[] selectedRepoConfigs) {
		this.repoUrls = selectedRepoConfigs;
	}

	public String getRepositoryUrl() {
		return repositoryUrl;
	}
//    public void setRepositoryUrl(String repositoryType) {
//        switch (repositoryType.toLowerCase()) {
//            case "mirror":
//                this.repositoryUrl = MIRROR_REPO_URL;
//                break;
//            case "custom":
//                // 自定义仓库URL应从首选项获取
//                this.repositoryUrl = Activator.getDefault().getPreferenceStore()
//                    .getString(RuyiPreferenceConstants.P_CUSTOM_REPOSITORY);
//                break;
//            default:
//                this.repositoryUrl = OFFICIAL_REPO_URL;
//        }
//    }

	public void setTelemetryEnabled(boolean enabled) {
		this.telemetryEnabled = enabled;
	}

	public boolean isTelemetryEnabled() {
		return telemetryEnabled;
	}

	// ========== 辅助方法 ==========

	public void cleanup() throws Exception {
		logger.logInfo("Cleaning up installation files");
		Path installDir = Paths.get(installPath);
		if (Files.exists(installDir)) {
			RuyiFileUtils.deleteRecursively(installDir);
		}
	}

	public boolean needsUpdate() throws Exception {
		String installed = getInstalledVersion();
		String latest = getLatestVersion();
		return installed != null && latest != null && !installed.equals(latest);
	}
}