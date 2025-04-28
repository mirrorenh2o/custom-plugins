package org.ruyisdk.ruyi.ui.status;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.ui.dialogs.RuyiErrorDialog;
import org.ruyisdk.ruyi.ui.dialogs.RuyiUpdateDialog;
import org.ruyisdk.ruyi.util.RuyiLogger;

public class RuyiStatusHandler {
    private static final RuyiLogger logger = Activator.getDefault().getLogger();
    
    public static void handleStatus(IStatus status) {
        Display.getDefault().asyncExec(() -> {
            switch (status.getSeverity()) {
                case IStatus.ERROR:
                    RuyiErrorDialog.openError(
                        "Error",
                        status.getMessage(),
                        status.getException() != null ? 
                            status.getException().getMessage() : null);
                    break;
                    
                case IStatus.WARNING:
                    MessageDialog.openWarning(
                        Display.getDefault().getActiveShell(),
                        "Warning",
                        status.getMessage());
                    break;
                    
                case IStatus.INFO:
                    // 特定信息处理
                    handleInfoStatus(status);
                    break;
                    
                default:
                    logger.logInfo("Status received: " + status.getMessage());
            }
        });
    }
    
    private static void handleInfoStatus(IStatus status) {
        // 根据状态码或消息内容进行特定处理
        if (status.getMessage().contains("Ruyi not installed")) {
            // 处理未安装情况
        } else if (status.getMessage().contains("Update available")) {
            // 处理更新可用情况
        }
    }
    
    public static void logAndShowError(String title, String message, Throwable exception) {
        logger.logError(message, exception);
        Display.getDefault().asyncExec(() -> 
            RuyiErrorDialog.openError(title, message, exception != null ? exception.getMessage() : null));
    }
}