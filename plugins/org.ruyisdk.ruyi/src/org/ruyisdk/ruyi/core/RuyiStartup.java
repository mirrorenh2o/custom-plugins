package org.ruyisdk.ruyi.core;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.jobs.CheckRuyiJob;
import org.ruyisdk.ruyi.util.RuyiLogger;

/**
 * Eclipse启动时自动执行的逻辑
 */
public class RuyiStartup implements IStartup {
    private static final RuyiLogger logger = Activator.getDefault().getLogger();

    @Override
    public void earlyStartup() {
        try {
            logger.logInfo("Ruyi SDK early startup initialized");
            
            // 延迟5秒后启动检查任务
            Display.getDefault().timerExec(5000, () -> {
                CheckRuyiJob job = new CheckRuyiJob();
                job.schedule();
            });
        } catch (Exception e) {
            logger.logError("Failed during early startup", e);
        }
    }
}