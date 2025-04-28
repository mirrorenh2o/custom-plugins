package org.ruyisdk.ruyi.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.util.RuyiFileUtils;

public class RuyiPreferenceInitializer extends AbstractPreferenceInitializer {
	// Eclipse框架调用的入口方法（非静态）
    @Override
    public void initializeDefaultPreferences() {
        doInitializeDefaultPreferences();
    }

    
 // 实际的初始化逻辑（静态方法）
    public static void doInitializeDefaultPreferences() {
    	//{workspace}/.metadata/.plugins/org.eclipse.core.runtime/.settings/{插件ID}.prefs  
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        
        // 安装配置
        store.setDefault(RuyiPreferenceConstants.P_INSTALL_PATH, 
            RuyiFileUtils.getDefaultInstallPath().toString());
        store.setDefault(RuyiPreferenceConstants.P_CHECK_ON_STARTUP, true);
        store.setDefault(RuyiPreferenceConstants.P_SKIP_VERSION_CHECK, false);
        
        // 版本信息
        store.setDefault(RuyiPreferenceConstants.P_CURRENT_VERSION, "0.0.0");
        store.setDefault(RuyiPreferenceConstants.P_LAST_VERSION_CHECK, 0L);
        
        // 存储库配置
        store.setDefault(RuyiPreferenceConstants.P_REPOSITORY, "official");
        store.setDefault(RuyiPreferenceConstants.P_CUSTOM_REPOSITORY, "");
        store.setDefault(RuyiPreferenceConstants.P_REPO_LAST_UPDATE, 0L);
        
        // 遥测配置
        store.setDefault(RuyiPreferenceConstants.P_TELEMETRY, "on");
        store.setDefault(RuyiPreferenceConstants.P_TELEMETRY_CLIENT_ID, 
            java.util.UUID.randomUUID().toString());
        
        // 高级配置
        store.setDefault(RuyiPreferenceConstants.P_DEBUG_MODE, false);
        store.setDefault(RuyiPreferenceConstants.P_LOG_LEVEL, "info");
    }
}