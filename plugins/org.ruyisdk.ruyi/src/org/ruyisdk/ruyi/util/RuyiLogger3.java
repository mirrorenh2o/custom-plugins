package org.ruyisdk.ruyi.util;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

public class RuyiLogger3 {
    private final Bundle bundle;
    
    public RuyiLogger3(Bundle bundle) {
        this.bundle = bundle;
    }
    
    public void log(int severity, String message) {
        log(severity, message, null);
    }
    
    public void log(int severity, String message, Throwable exception) {
        Platform.getLog(bundle).log(
            new Status(severity, bundle.getSymbolicName(), message, exception)
        );
    }
    
    // 便捷方法
    public void logError(String message, Throwable ex) {
        log(IStatus.ERROR, message, ex);
    }
    
    public void logInfo(String message) {
        log(IStatus.INFO, message);
    }
}