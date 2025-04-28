package org.ruyisdk.core.ruyi.commands;

import org.ruyisdk.core.ruyi.RuyiException;

/**
 * 遥测相关命令
 */
public class TelemetryCommand extends RuyiCommand {
    public String getStatus() throws RuyiException {
        return executeCommand("ruyi telemetry status");
    }

    public void enable() throws RuyiException {
        executeCommand("ruyi telemetry consent");
    }

    public void disable() throws RuyiException {
        executeCommand("ruyi telemetry optout");
    }

    public void upload() throws RuyiException {
        executeCommand("ruyi telemetry upload");
    }
}