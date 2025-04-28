package org.ruyisdk.core.basedir;

import java.nio.file.Path;
import org.ruyisdk.core.config.Constants;

public class TestDir {

    
    public static void main(String[] args) {
        String appName = Constants.AppInfo.AppDir;

        Path configDir = DirUtil.getConfigDir(appName);
        Path cacheDir = DirUtil.getCacheDir(appName);

        System.out.println("Config Dir: " + configDir);
        System.out.println("Cache Dir: " + cacheDir);

        // 确保目录存在
        configDir.toFile().mkdirs();
        cacheDir.toFile().mkdirs();
    }

}
