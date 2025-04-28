package org.ruyisdk.ruyi.core;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.util.RuyiLogger;

/**
 * Ruyi SDK主应用程序入口
 */
public class RuyiApplication implements IApplication {
    private static final RuyiLogger logger = Activator.getDefault().getLogger();

    @Override
    public Object start(IApplicationContext context) throws Exception {
        logger.logInfo("Starting Ruyi SDK Application");
        
        // 初始化核心服务
        RuyiCore core = Activator.getDefault().getRuyiCore();
        core.startBackgroundJobs();
        
        return IApplication.EXIT_OK;
    }

    @Override
    public void stop() {
        logger.logInfo("Stopping Ruyi SDK Application");
        Activator.getDefault().getRuyiCore().shutdown();
    }
}