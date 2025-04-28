package org.ruyisdk.ruyi.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.jobs.CheckRuyiJob;
import org.ruyisdk.ruyi.preferences.RuyiPreferenceConstants;
import org.ruyisdk.ruyi.ui.dialogs.RuyiInstallWizard;
import org.ruyisdk.ruyi.util.RuyiLogger;

public class RuyiCore {
    private final RuyiLogger logger;
    private RuyiInstallManager installManager;
    private RuyiVersionChecker versionChecker;

    public RuyiCore(RuyiLogger logger) {
        this.logger = logger;
        this.installManager = new RuyiInstallManager(logger);
        this.versionChecker = new RuyiVersionChecker(logger);
    }

    public void startBackgroundJobs() {
    	logger.logInfo("Starting Ruyi core background jobs");
        if (shouldCheckAtStartup()) {
            scheduleInitialCheckJob();
        }
    }

    private boolean shouldCheckAtStartup() {
        IPreferenceStore prefs = Activator.getDefault().getPreferenceStore();
        boolean skipCheck = prefs.getBoolean(RuyiPreferenceConstants.P_SKIP_VERSION_CHECK);
        return !skipCheck && prefs.getBoolean(RuyiPreferenceConstants.P_CHECK_ON_STARTUP);
    }

    private void scheduleInitialCheckJob() {
        Job.create("Initial Ruyi Environment Check", monitor -> {
            CheckRuyiJob checkJob = new CheckRuyiJob();
            IStatus status = checkJob.run(monitor);
            
            if (status.getSeverity() == IStatus.ERROR) {
                logger.logError("Ruyi environment check failed", status.getException());
                showErrorDialog("Ruyi Check Failed", status.getMessage());
            } else if (status.getSeverity() == IStatus.INFO) {
                Display.getDefault().asyncExec(() -> {
                    RuyiInstallWizard wizard = new RuyiInstallWizard();
                    wizard.open();
                });
            }
            
            return status;
        }).schedule(2000); // 延迟2秒启动
    }

    private void showErrorDialog(String title, String message) {
        Display.getDefault().asyncExec(() -> 
            MessageDialog.openError(Display.getDefault().getActiveShell(), title, message));
    }

    public void shutdown() {
        logger.logInfo("Shutting down Ruyi core services");
        // 其他清理工作
    }

    public RuyiInstallManager getInstallManager() {
        return installManager;
    }

    public RuyiVersionChecker getVersionChecker() {
        return versionChecker;
    }
}