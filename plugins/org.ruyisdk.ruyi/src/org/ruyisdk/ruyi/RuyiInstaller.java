package org.ruyisdk.ruyi;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.ruyisdk.core.config.Constants;
import org.ruyisdk.core.ruyi.model.RuyiManager;
import org.ruyisdk.core.ruyi.model.RuyiVersion;
import org.ruyisdk.core.ruyi.model.SystemInfo;
import org.eclipse.core.runtime.IProgressMonitor;

public class RuyiInstaller {
    
    public static boolean installRuyi(String installPath, IProgressMonitor monitor) {
        try {
            monitor.beginTask("安装 Ruyi", 6);
            
            // 1. 解析路径
            Path installDir = Paths.get(installPath.replaceFirst("^~", System.getProperty("user.home")));
            monitor.worked(1);
            
            // 2. 检查并备份现有安装
            Path ruyiPath = installDir.resolve("ruyi");
            if (Files.exists(ruyiPath)) {
                String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                Path backupPath = installDir.resolve(Constants.Ruyi.BACKUP_PREFIX + timestamp);
                Files.move(ruyiPath, backupPath);
                monitor.worked(1);
            } else {
                monitor.worked(1);
            }
            
            // 3. 获取最新版本
            RuyiVersion latestVersion = RuyiManager.getLatestVersion();
            if (latestVersion == null) {
                throw new IOException("无法获取最新版本信息");
            }
            System.out.print("latestVersion: "+latestVersion);
            monitor.worked(1);
            
            // 4. 下载安装包
            String arch = SystemInfo.detectArchitecture().getSuffix();
            String downloadUrl = Constants.NetAddress.MIRROR_RUYI_RELEASES + 
                latestVersion + "/ruyi." + arch;
            System.out.print("downloadUrl: "+downloadUrl);
            Path tempFile = Files.createTempFile("ruyi", ".download");
            
            try (InputStream in = new URL(downloadUrl).openStream()) {
                Files.copy(in, tempFile, StandardCopyOption.REPLACE_EXISTING);
            }
            monitor.worked(1);
            
            // 5. 安装并设置权限
            Files.move(tempFile, ruyiPath);
            ruyiPath.toFile().setExecutable(true);
            monitor.worked(1);
            
            // 6. 验证安装
            Process process = new ProcessBuilder(ruyiPath.toString(), "-V").start();
            if (process.waitFor() != 0) {
                throw new IOException("安装验证失败");
            }
            monitor.worked(1);
            
            return true;
        } catch (Exception e) {
//            Activator.logError("安装 Ruyi SDK 失败", e);
            System.out.print("\"安装 Ruyi SDK 失败\" ");
            return false;
        } finally {
            monitor.done();
        }
    }
}