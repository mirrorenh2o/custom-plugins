package org.ruyisdk.ruyi.preferences;

public interface RuyiPreferenceConstants {
    // 安装配置
    String P_INSTALL_PATH = "ruyi.install.path";
    String P_CHECK_ON_STARTUP = "ruyi.check.on.startup";
    String P_SKIP_VERSION_CHECK = "ruyi.skip.version.check";
    
    // 版本信息
    String P_CURRENT_VERSION = "ruyi.current.version";
    String P_LAST_VERSION_CHECK = "ruyi.last.version.check";
    
    // 存储库配置
    String P_REPOSITORY = "ruyi.repository";
    String P_CUSTOM_REPOSITORY = "ruyi.custom.repository";
    String P_REPO_LAST_UPDATE = "ruyi.repo.last.update";
    
    // 遥测配置
    String P_TELEMETRY = "ruyi.telemetry";
    String P_TELEMETRY_CLIENT_ID = "ruyi.telemetry.client.id";
    
    // 高级配置
    String P_DEBUG_MODE = "ruyi.debug.mode";
    String P_LOG_LEVEL = "ruyi.log.level";
}