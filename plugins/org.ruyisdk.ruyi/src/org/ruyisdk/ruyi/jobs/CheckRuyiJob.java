package org.ruyisdk.ruyi.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.widgets.Display;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.core.RuyiVersionChecker;
import org.ruyisdk.ruyi.core.VersionCheckResult;
import org.ruyisdk.ruyi.ui.dialogs.RuyiInstallWizard;
import org.ruyisdk.ruyi.ui.dialogs.RuyiUpdateDialog;
import org.ruyisdk.ruyi.util.RuyiLogger;

public class CheckRuyiJob extends Job {
    private static final String PLUGIN_ID = "org.ruyisdk.ruyi";
    private final RuyiLogger logger;
    
    public CheckRuyiJob() {
        super("Checking Ruyi Installation");
        this.logger = Activator.getDefault().getLogger();
        setUser(true); // 显示在进度对话框中
        setPriority(Job.SHORT); // 高优先级任务
    }

    @Override
	public IStatus run(IProgressMonitor monitor) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, "Checking Ruyi environment", 3);
        
        try {
            // 阶段1: 检查安装状态 (1/3工作量)
            subMonitor.subTask("Checking installation status");
            boolean isInstalled = checkInstallation(subMonitor.split(1));
            
            if (!isInstalled) {
                logger.logInfo("Ruyi is not installed");
                return promptInstallation();
            }
            
            // 阶段2: 检查版本信息 (1/3工作量)
            subMonitor.subTask("Checking version information");
            VersionCheckResult versionResult = checkVersion(subMonitor.split(1));
            
            // 阶段3: 处理检查结果 (1/3工作量)
            subMonitor.subTask("Processing results");
            return handleVersionResult(versionResult, subMonitor.split(1));
        } catch (Exception e) {
            logger.logError("Error during Ruyi check", e);
            return new Status(IStatus.ERROR, PLUGIN_ID, "Failed to check Ruyi status", e);
        } finally {
            if (monitor != null) {
                monitor.done();
            }
        }
    }

    private boolean checkInstallation(IProgressMonitor monitor) throws Exception {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        try {
            return Activator.getDefault()
                .getRuyiCore()
                .getInstallManager()
                .isInstalled();
        } finally {
            subMonitor.done();
        }
    }

    private VersionCheckResult checkVersion(IProgressMonitor monitor) throws Exception {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        try {
            return Activator.getDefault()
                .getRuyiCore()
                .getVersionChecker()
                .checkVersion(subMonitor);
        } finally {
            subMonitor.done();
        }
    }

    private IStatus promptInstallation() {
        Display.getDefault().asyncExec(() -> {
            RuyiInstallWizard wizard = new RuyiInstallWizard();
            wizard.open();
        });
        return new Status(IStatus.INFO, PLUGIN_ID, "Ruyi not installed, launching wizard");
    }

    private IStatus handleVersionResult(VersionCheckResult result, IProgressMonitor monitor) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, 1);
        try {
            if (result.needsUpdate()) {
                Display.getDefault().asyncExec(() -> {
                    RuyiUpdateDialog dialog = new RuyiUpdateDialog(
                        Display.getDefault().getActiveShell(), 
                        result.getLocalVersion(), 
                        result.getLatestVersion()
//                        result.getChangelog()
                        );
                    dialog.open();
                });
//             // 在CheckRuyiJob的handleVersionResult方法中：
//                Display.getDefault().asyncExec(() -> {
//                    MessageDialog.openInformation(
//                        Display.getDefault().getActiveShell(),
//                        "Ruyi Check Complete",
//                        result.getMessage());
//                });
                return new Status(IStatus.INFO, PLUGIN_ID, 
                    "Update available: " + result.getMessage());
            }
            
            logger.logInfo("Ruyi version check completed: " + result.getMessage());
            return Status.OK_STATUS;
        } finally {
            subMonitor.done();
        }
    }

    @Override
    protected void canceling() {
        super.canceling();
        logger.logInfo("Ruyi installation check was cancelled");
    }
}