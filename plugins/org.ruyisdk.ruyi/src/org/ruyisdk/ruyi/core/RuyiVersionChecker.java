package org.ruyisdk.ruyi.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.preferences.RuyiPreferenceConstants;
import org.ruyisdk.ruyi.util.RuyiFileUtils;
import org.ruyisdk.ruyi.util.RuyiLogger;
import org.ruyisdk.ruyi.util.RuyiNetworkUtils;

public class RuyiVersionChecker {
    private static final Pattern VERSION_PATTERN = Pattern.compile("ruyi version (\\d+\\.\\d+\\.\\d+)");
    private static final String VERSION_FILE = "VERSION";
    private static final String CHANGELOG_FILE = "CHANGELOG.md";
    
    private final RuyiLogger logger;
    private String repositoryUrl;

    public RuyiVersionChecker(RuyiLogger logger) {
        this.logger = logger;
        this.repositoryUrl = Activator.getDefault().getPreferenceStore()
            .getString(RuyiPreferenceConstants.P_REPOSITORY).equals("mirror") ? 
                "https://mirror.iscas.ruyisdk.com" : "https://repo.ruyisdk.com";
    }

    /**
     * 检查Ruyi版本状态
     */
    public VersionCheckResult checkVersion(IProgressMonitor monitor) throws Exception {
        SubMonitor subMonitor = SubMonitor.convert(monitor, "Checking Ruyi version", 3);
        
        try {
            // 阶段1: 检查本地安装 (1/3工作量)
            subMonitor.subTask("Checking local installation");
            String localVersion = getLocalVersion(subMonitor.split(1));
            
            // 阶段2: 获取远程版本 (1/3工作量)
            subMonitor.subTask("Fetching remote version");
            String latestVersion = getLatestVersion(subMonitor.split(1));
            
            // 阶段3: 比较版本 (1/3工作量)
            subMonitor.subTask("Comparing versions");
            return compareVersions(localVersion, latestVersion, subMonitor.split(1));
        } finally {
            subMonitor.done();
        }
    }

    /**
     * 获取本地安装版本
     */
    public String getLocalVersion(IProgressMonitor monitor) throws IOException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        try {
            String installPath = Activator.getDefault().getPreferenceStore()
                .getString(RuyiPreferenceConstants.P_INSTALL_PATH);
            Path versionFile = Paths.get(installPath, VERSION_FILE);
            
            if (!Files.exists(versionFile)) {
                logger.logInfo("No local version file found at: " + versionFile);
                return null;
            }
            
            String content = RuyiFileUtils.readFileContent(versionFile.toString());
            Matcher matcher = VERSION_PATTERN.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return content.trim(); // 回退到直接读取文件内容
        } finally {
            subMonitor.done();
        }
    }

    /**
     * 获取最新远程版本
     */
    public String getLatestVersion(IProgressMonitor monitor) throws IOException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        try {
            String versionUrl = repositoryUrl + "/latest/version";
            return RuyiNetworkUtils.fetchStringContent(versionUrl, subMonitor);
        } finally {
            subMonitor.done();
        }
    }

    /**
     * 获取指定版本的更新日志
     */
    public String getChangelog(String version, IProgressMonitor monitor) throws IOException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        try {
            String changelogUrl = repositoryUrl + "/changelog/" + version;
            return RuyiNetworkUtils.fetchStringContent(changelogUrl, subMonitor);
        } catch (IOException e) {
            logger.logWarning("Failed to fetch changelog for version: " + version, e);
            return "No changelog available";
        } finally {
            subMonitor.done();
        }
    }

    /**
     * 比较版本并生成结果
     */
    private VersionCheckResult compareVersions(String localVersion, String latestVersion, 
                                            IProgressMonitor monitor) throws IOException {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        try {
            // 情况1: 未安装
            if (localVersion == null || localVersion.isEmpty()) {
                return VersionCheckResult.notInstalled();
            }
            
            // 情况2: 获取远程版本失败
            if (latestVersion == null || latestVersion.isEmpty()) {
                return VersionCheckResult.checkFailed("Could not fetch latest version");
            }
            
            // 情况3: 已是最新版本
            if (isVersionUpToDate(localVersion, latestVersion)) {
                return VersionCheckResult.upToDate(localVersion);
            }
            
            // 情况4: 需要更新
            String changelog = getChangelog(latestVersion, subMonitor);
            return VersionCheckResult.needsUpdate(localVersion, latestVersion, changelog);
        } finally {
            subMonitor.done();
        }
    }

    /**
     * 比较版本号是否最新
     */
    private boolean isVersionUpToDate(String localVersion, String latestVersion) {
        String[] localParts = localVersion.split("\\.");
        String[] latestParts = latestVersion.split("\\.");
        
        for (int i = 0; i < Math.max(localParts.length, latestParts.length); i++) {
            int localNum = i < localParts.length ? Integer.parseInt(localParts[i]) : 0;
            int latestNum = i < latestParts.length ? Integer.parseInt(latestParts[i]) : 0;
            
            if (localNum < latestNum) {
                return false; // 需要更新
            } else if (localNum > latestNum) {
                return true; // 本地版本更高(可能是开发版)
            }
        }
        return true; // 版本完全相同
    }

    /**
     * 设置仓库镜像地址
     */
    public void setRepositoryUrl(String repositoryType) {
        this.repositoryUrl = repositoryType.equals("mirror") ? 
            "https://mirror.iscas.ruyisdk.com" : "https://repo.ruyisdk.com";
    }
}