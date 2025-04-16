package org.ruyisdk.core.basedir;

import java.nio.file.Path;
import java.nio.file.Paths;

public class WindowsDirs {

    /**
     * 获取 Windows 的配置目录（%APPDATA%\<app-name>）
     */
    public static Path getConfigDir(String appName) {
        String appData = System.getenv("APPDATA");
        if (appData == null) {
            appData = System.getProperty("user.home") + "\\AppData\\Roaming";
        }
        return Paths.get(appData, appName);
    }

    /**
     * 获取 Windows 的缓存目录（%LOCALAPPDATA%\<app-name>\Cache）
     */
    public static Path getCacheDir(String appName) {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null) {
            localAppData = System.getProperty("user.home") + "\\AppData\\Local";
        }
        return Paths.get(localAppData, appName, "Cache");
    }
    
    /**
     * 获取 Windows 的数据目录（%LOCALAPPDATA%\<app-name>\Data）
     */
    public static Path getDataDir(String appName) {
        String localAppData = System.getenv("LOCALAPPDATA");
        if (localAppData == null) {
            localAppData = System.getProperty("user.home") + "\\AppData\\Local";
        }
        return Paths.get(localAppData, appName, "Data");
    }
}