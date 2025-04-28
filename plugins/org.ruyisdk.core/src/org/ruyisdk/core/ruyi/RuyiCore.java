package org.ruyisdk.core.ruyi;

import org.eclipse.core.runtime.IProgressMonitor;
import org.ruyisdk.core.ruyi.commands.InstallCommand;
import org.ruyisdk.core.ruyi.commands.TelemetryCommand;
import org.ruyisdk.core.ruyi.commands.VersionCommand;
import org.ruyisdk.core.ruyi.model.RuyiVersion;

/**
 * Ruyi SDK 核心功能类
 */
public class RuyiCore {
    private static RuyiCore instance;
    private final TelemetryCommand telemetryCommand = new TelemetryCommand();

    private RuyiCore() {
        // 私有构造函数
    }

    public static synchronized RuyiCore getInstance() {
        if (instance == null) {
            instance = new RuyiCore();
        }
        return instance;
    }

    public boolean isInstalled() {
        return VersionCommand.checkInstalled();
    }

    public String getLocalVersion() throws RuyiException {
        return VersionCommand.getLocalVersion();
    }

    public String getLatestVersion() throws RuyiException {
        return VersionCommand.getLatestRemoteVersion();
    }

    public boolean isUpdateAvailable() throws RuyiException {
        if (!isInstalled()) {
            return true;
        }
        RuyiVersion local = RuyiVersion.parse(getLocalVersion());
        RuyiVersion remote = RuyiVersion.parse(getLatestVersion());
        return remote.compareTo(local) > 0;
    }

    public void installLatest(IProgressMonitor monitor) throws RuyiException {
        InstallCommand.installLatest(monitor);
    }

    public String getTelemetryStatus() throws RuyiException {
        return telemetryCommand.getStatus();
    }

    public void enableTelemetry() throws RuyiException {
        telemetryCommand.enable();
    }

    public void disableTelemetry() throws RuyiException {
        telemetryCommand.disable();
    }

    public void uploadTelemetry() throws RuyiException {
        telemetryCommand.upload();
    }
}