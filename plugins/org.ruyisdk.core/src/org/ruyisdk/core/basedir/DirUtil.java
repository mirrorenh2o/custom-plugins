package org.ruyisdk.core.basedir;

import java.nio.file.Path;
import java.nio.file.Paths;

public class DirUtil {

    /**
     * 获取当前平台的配置目录
     */
    public static Path getConfigDir(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return WindowsDirs.getConfigDir(appName);
        } else if (os.contains("mac")) {
            return MacDirs.getConfigDir(appName);
        } else {
            return XDGDirs.getConfigDir(appName); // Linux/Unix
        }
    }

    /**
     * 获取当前平台的缓存目录
     */
    public static Path getCacheDir(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return WindowsDirs.getCacheDir(appName);
        } else if (os.contains("mac")) {
            return MacDirs.getCacheDir(appName);
        } else {
            return XDGDirs.getCacheDir(appName); // Linux/Unix
        }
    }
}