package org.ruyisdk.ruyi.jobs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.core.RuyiInstallManager;
import org.ruyisdk.ruyi.preferences.RuyiPreferenceConstants;
import org.ruyisdk.ruyi.util.RuyiLogger;

public class UpdateRuyiJob extends Job {
    private static final String PLUGIN_ID = "org.ruyisdk.ruyi";
    private final RuyiLogger logger;
    private final String targetVersion;
    
    public UpdateRuyiJob(String targetVersion) {
        super("Updating Ruyi SDK");
        this.logger = Activator.getDefault().getLogger();
        this.targetVersion = targetVersion;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        SubMonitor subMonitor = SubMonitor.convert(monitor, "Updating Ruyi SDK", 100);
        
        try {
            subMonitor.subTask("Preparing update");
            RuyiInstallManager installManager = Activator.getDefault()
                .getRuyiCore().getInstallManager();
            subMonitor.worked(10);
            
            subMonitor.subTask("Downloading update");
            downloadUpdate(installManager, subMonitor.newChild(50));
            
            subMonitor.subTask("Installing update");
            installUpdate(installManager, subMonitor.newChild(30));
            
            subMonitor.subTask("Finalizing update");
            finalizeUpdate(subMonitor.newChild(10));
            
            notifySuccess();
            return Status.OK_STATUS;
        } catch (Exception e) {
            logger.logError("Failed to update Ruyi", e);
            notifyFailure(e.getMessage());
            return new Status(IStatus.ERROR, PLUGIN_ID, "Update failed", e);
        } finally {
            subMonitor.done();
        }
    }

    private void downloadUpdate(RuyiInstallManager installManager, IProgressMonitor monitor) 
            throws Exception {
        // 实现下载逻辑
        monitor.subTask("Downloading version " + targetVersion);
        // 模拟下载进度
        for (int i = 0; i < 100 && !monitor.isCanceled(); i++) {
            Thread.sleep(50);
            monitor.worked(1);
        }
        
        if (monitor.isCanceled()) {
            throw new InterruptedException("Update cancelled by user");
        }
    }

    private void installUpdate(RuyiInstallManager installManager, IProgressMonitor monitor) 
            throws Exception {
        // 实现安装逻辑
        monitor.subTask("Installing files");
        // 模拟安装进度
        for (int i = 0; i < 100 && !monitor.isCanceled(); i++) {
            Thread.sleep(30);
            monitor.worked(1);
        }
    }

    private void finalizeUpdate(IProgressMonitor monitor) {
        // 更新首选项中的版本信息
        Activator.getDefault().getPreferenceStore()
            .setValue(RuyiPreferenceConstants.P_CURRENT_VERSION, targetVersion);
    }

    private void notifySuccess() {
        Display.getDefault().asyncExec(() -> 
            MessageDialog.openInformation(
                Display.getDefault().getActiveShell(),
                "Update Complete",
                "Ruyi SDK has been successfully updated to version " + targetVersion));
    }

    private void notifyFailure(String message) {
        Display.getDefault().asyncExec(() -> 
            MessageDialog.openError(
                Display.getDefault().getActiveShell(),
                "Update Failed",
                "Failed to update Ruyi SDK: " + message));
    }
}