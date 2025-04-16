package org.ruyisdk.core.basedir;

import java.nio.file.Path;
import java.nio.file.Paths;

public class MacDirs {

    /**
     * 获取 macOS 的配置目录（~/Library/Preferences/<app-name>）
     */
    public static Path getConfigDir(String appName) {
        String home = System.getProperty("user.home");
        return Paths.get(home, "Library", "Preferences", appName);
    }

    /**
     * 获取 macOS 的缓存目录（~/Library/Caches/<app-name>）
     */
    public static Path getCacheDir(String appName) {
        String home = System.getProperty("user.home");
        return Paths.get(home, "Library", "Caches", appName);
    }
    
    /**
     * 获取 macOS 的数据目录（~/Library/Application Support/<app-name>）
     */
    public static Path getDataDir(String appName) {
        String home = System.getProperty("user.home");
        return Paths.get(home, "Library", "Application Support", appName);
    }
}
