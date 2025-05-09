package org.ruyisdk.ruyi;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ruyisdk.core.ruyi.model.RuyiVersion;
import org.ruyisdk.ruyi.services.RuyiManager;
import org.ruyisdk.ruyi.views.RuyiInstallDialog;
import org.eclipse.jface.window.Window;

public class RuyiStartupCheckJob extends Job {
    
    public RuyiStartupCheckJob() {
        super("检查 Ruyi SDK");
    }
    
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        try {
            monitor.beginTask("检查 Ruyi 安装状态", 3);
            
            // 1. 检查是否安装
            if (!RuyiManager.isRuyiInstalled()) {
                monitor.worked(1);
                return installOrUpdate(monitor, null);
            }
            monitor.worked(1);
            
            // 2. 检查版本
            RuyiVersion installedVersion = RuyiManager.getInstalledVersion();
            RuyiVersion latestVersion = RuyiManager.getLatestVersion();
            monitor.worked(1);
            
            if (latestVersion == null) {
                return new Status(IStatus.WARNING, Activator.PLUGIN_ID, "无法获取最新版本信息");
            }
            
            if (installedVersion.compareTo(latestVersion) < 0) {
                return installOrUpdate(monitor, latestVersion);
            }
            
            monitor.worked(1);
            Activator.getDefault().getLogger().logInfo("已安装最新版 Ruyi: " + installedVersion);
            return Status.OK_STATUS;
        } finally {
            monitor.done();
        }
    }
    
    private IStatus installOrUpdate(IProgressMonitor monitor, RuyiVersion targetVersion) {
        Display.getDefault().syncExec(() -> {
            RuyiInstallDialog dialog = new RuyiInstallDialog(Display.getDefault().getActiveShell());
            if (dialog.open() == Window.OK) {
                String installPath = dialog.getInstallPath();
                boolean success = RuyiInstaller.installRuyi(installPath, monitor);
                
                if (success) {
                    MessageDialog.openInformation(
                        Display.getDefault().getActiveShell(),
                        "安装成功",
                        targetVersion == null ? 
                            "Ruyi SDK 安装成功" :
                            "Ruyi SDK 已更新至版本 " + targetVersion
                    );
                } else {
                    MessageDialog.openError(
                        Display.getDefault().getActiveShell(),
                        "安装失败",
                        "Ruyi SDK 安装失败，请查看错误日志"
                    );
                }
            }
        });
        return Status.OK_STATUS;
    }
}
