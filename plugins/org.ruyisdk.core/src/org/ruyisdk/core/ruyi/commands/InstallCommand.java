package org.ruyisdk.core.ruyi.commands;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ruyisdk.core.ruyi.RuyiException;
import org.ruyisdk.core.ruyi.model.SystemInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 安装相关命令
 */
public class InstallCommand extends RuyiCommand {
    private static final String INSTALL_PATH = "/usr/local/bin/ruyi";
    private static final String DOWNLOAD_URL_TEMPLATE = 
        "https://mirror.iscas.ac.cn/ruyisdk/ruyi/releases/%s/ruyi.%s";

    public static void installLatest(IProgressMonitor monitor) throws RuyiException {
        InstallCommand cmd = new InstallCommand();
        String version = VersionCommand.getLatestRemoteVersion();
        cmd.installVersion(version, monitor);
    }

    public void installVersion(String version, IProgressMonitor monitor) throws RuyiException {
        try {
            monitor.beginTask("Installing Ruyi " + version, 100);
            
            // 1. 备份现有文件
            backupExistingFile(monitor);
            monitor.worked(10);
            
            // 2. 下载新版本
            String downloadUrl = getDownloadUrl(version);
            downloadFile(downloadUrl, INSTALL_PATH, monitor);
            monitor.worked(70);
            
            // 3. 设置权限
            setExecutablePermission(monitor);
            monitor.worked(20);
            
        } catch (Exception e) {
            throw new RuyiException(RuyiException.ErrorCode.COMMAND_EXECUTION_FAILED,
                "Installation failed", e);
        } finally {
            monitor.done();
        }
    }

    private void backupExistingFile(IProgressMonitor monitor) {
        File file = new File(INSTALL_PATH);
        if (file.exists()) {
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            File backup = new File(INSTALL_PATH + "." + timestamp);
            file.renameTo(backup);
        }
    }

    private String getDownloadUrl(String version) throws RuyiException {
        SystemInfo.Architecture arch = SystemInfo.detectArchitecture();
        if (arch == SystemInfo.Architecture.UNKNOWN) {
            throw new RuyiException(RuyiException.ErrorCode.UNSUPPORTED_ARCHITECTURE,
                "Unsupported architecture: " + System.getProperty("os.arch"));
        }
        return String.format(DOWNLOAD_URL_TEMPLATE, version, arch.getSuffix());
    }

    private void downloadFile(String url, String destPath, IProgressMonitor monitor) 
            throws IOException {
        URL downloadUrl = new URL(url);
        ReadableByteChannel rbc = Channels.newChannel(downloadUrl.openStream());
        try (FileOutputStream fos = new FileOutputStream(destPath)) {
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
        }
    }

    private void setExecutablePermission(IProgressMonitor monitor) throws IOException, InterruptedException {
        Process chmod = Runtime.getRuntime().exec("chmod +x " + INSTALL_PATH);
        chmod.waitFor();
    }
}