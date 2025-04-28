package org.ruyisdk.ruyi.telemetry;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.preferences.RuyiPreferenceConstants;
import org.ruyisdk.ruyi.util.RuyiLogger;
import org.ruyisdk.ruyi.util.RuyiNetworkUtils;

public class RuyiTelemetryManager {
    private static final String TELEMETRY_URL = "https://telemetry.ruyisdk.com/api/v1/events";
    private final RuyiLogger logger;
    
    public RuyiTelemetryManager(RuyiLogger logger) {
        this.logger = logger;
    }
    
    public void sendEvent(String eventName, Map<String, Object> properties) {
        if (!isTelemetryEnabled()) {
            return;
        }
        
        Job.create("Send Telemetry Event", monitor -> {
            try {
                Map<String, Object> payload = new HashMap<>();
                payload.put("event", eventName);
                payload.put("timestamp", System.currentTimeMillis());
                payload.put("properties", properties);
                payload.put("client_id", getClientId());
                
                RuyiNetworkUtils.postJson(TELEMETRY_URL, payload, monitor);
                return Status.OK_STATUS;
            } catch (Exception e) {
                logger.logWarning("Failed to send telemetry event: " + eventName, e);
                return new Status(IStatus.WARNING, Activator.PLUGIN_ID, 
                    "Telemetry send failed", e);
            }
        }).schedule();
    }
    
    private boolean isTelemetryEnabled() {
        String setting = Activator.getDefault().getPreferenceStore()
            .getString(RuyiPreferenceConstants.P_TELEMETRY);
        return !"off".equals(setting);
    }
    
    private String getClientId() {
        if ("anonymous".equals(Activator.getDefault().getPreferenceStore()
            .getString(RuyiPreferenceConstants.P_TELEMETRY))) {
            return "anonymous";
        }
        return Activator.getDefault().getPreferenceStore()
            .getString(RuyiPreferenceConstants.P_TELEMETRY_CLIENT_ID);
    }
    
    public void sendStartupEvent() {
        Map<String, Object> props = new HashMap<>();
        props.put("os", System.getProperty("os.name"));
        props.put("java_version", System.getProperty("java.version"));
        sendEvent("ide_startup", props);
    }
    
    public void sendInstallationEvent(boolean success, String version) {
        Map<String, Object> props = new HashMap<>();
        props.put("success", success);
        props.put("version", version);
        sendEvent("installation", props);
    }
}