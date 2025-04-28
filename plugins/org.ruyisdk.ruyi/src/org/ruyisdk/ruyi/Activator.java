package org.ruyisdk.ruyi;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.ruyisdk.ruyi.core.RuyiCore;
import org.ruyisdk.ruyi.preferences.RuyiPreferenceInitializer;
import org.ruyisdk.ruyi.util.RuyiLogger;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.preference.IPreferenceStore;

public class Activator extends AbstractUIPlugin {
    
    public static final String PLUGIN_ID = "org.ruyisdk.ruyi";
    private static Activator plugin;
    private RuyiCore ruyiCore;
    private RuyiLogger logger;
    
    public Activator() {
        plugin = this;
    }
    
    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;
        
        logger = new RuyiLogger(getLog());
        
        try {
        	// 初始化首选项（调用静态方法）
            RuyiPreferenceInitializer.doInitializeDefaultPreferences();
            
            // 初始化核心组件
            ruyiCore = new RuyiCore(logger);
            ruyiCore.startBackgroundJobs();
            
            logger.logInfo("Ruyi plugin activated successfully");
        } catch (Exception e) {
            logger.logError("Failed to activate Ruyi SDK plugin", e);
            throw e;
        }
        
    }
    
    @Override
    public void stop(BundleContext context) throws Exception {
//        ruyiCore.shutdown();
//        plugin = null;
//        super.stop(context);
        try {
            if (ruyiCore != null) {
                ruyiCore.shutdown();
            }
            logger.logInfo("Ruyi plugin deactivated");
        } catch (Exception e) {
            logger.logError("Error during plugin deactivation", e);
        } finally {
            plugin = null;
            super.stop(context);
        }
    }
      
    public static Activator getDefault() {
        return plugin;
    }
    
    public RuyiCore getRuyiCore() {
        return ruyiCore;
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        return super.getPreferenceStore();
    }
    
    public RuyiLogger getLogger() {
        return logger;
    }
    
    public static void initializeImageRegistry() {
        // 示例图片注册 (实际路径需要调整)
    	Activator.getDefault().getImageRegistry().put("folder", 
            imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/folder.png"));
    	Activator.getDefault().getImageRegistry().put("version", 
            imageDescriptorFromPlugin(Activator.PLUGIN_ID, "icons/version.png"));
        // 其他图标...
    }
}
