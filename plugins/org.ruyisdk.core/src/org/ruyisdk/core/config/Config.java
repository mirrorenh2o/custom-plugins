package org.ruyisdk.core.config;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.ruyisdk.core.basedir.XDGDirs;
import org.ruyisdk.core.console.RuyiSDKConsole;

public class Config {
//	Path userConfigPath = Paths.get(System.getProperty("user.home"), ".ruyi", Constants.ConfigFile.RuyiProperties);  //"ruyi.properties"
	private static final Path FILE_PATH = Paths.get(
			XDGDirs.getConfigDir(Constants.AppInfo.AppDir).toString(), 
			Constants.ConfigFile.RuyiProperties     //"ruyi.properties"
		); 
	private static final Properties userProps = loadUserConfig();

	// 加载用户配置
	private static Properties loadUserConfig() {
		Properties props = new Properties();
		
		if (Files.exists(FILE_PATH)) {
			try (InputStream is = Files.newInputStream(FILE_PATH)) {
				props.load(is);
			} catch (IOException e) {
//				Logger.warn("Failed to load user config", e);
				RuyiSDKConsole.getInstance().logError("Failed to load user config");
				System.out.print(e);
			}
		}
		return props;
	}
	
	// 获取安装路径（用户配置优先，常量默认值兜底）
	public static String getInstallPath() {
		return userProps.getProperty("ruyi.install.path", Constants.Ruyi.INSTALL_PATH);
	}
	
}
