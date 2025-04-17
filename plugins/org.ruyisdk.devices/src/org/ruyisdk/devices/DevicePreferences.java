package org.ruyisdk.devices;

import org.eclipse.core.runtime.preferences.*;
import org.osgi.service.prefs.BackingStoreException;
import org.ruyisdk.core.console.RuyiSDKConsole;

public class DevicePreferences {
    private static final String PREF_DEVICES = "ruyisdk.devices";
    private static final String DEFAULT_DEVICES = "[]";
    
    // 添加公共访问方法
    public static String getPreferenceKey() {
        return PREF_DEVICES;
    }

    public static String getDefaultValue() {
        return DEFAULT_DEVICES;
    }
    
    public static String getDevicesJson() {
    	return InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID)
                .get(PREF_DEVICES, DEFAULT_DEVICES);
    }

    public static void saveDevicesJson(String json) {
    	IEclipsePreferences prefs = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        prefs.put(PREF_DEVICES, json);
        try {
            prefs.flush();
        } catch (BackingStoreException e) {
        	RuyiSDKConsole.getInstance().logError("Failed to save device preferences");
        }
    }
}