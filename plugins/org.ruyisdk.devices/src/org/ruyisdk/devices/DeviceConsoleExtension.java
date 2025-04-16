package org.ruyisdk.devices;

import org.ruyisdk.core.console.ConsoleExtensions.ConsoleExtension;
import org.ruyisdk.core.console.RuyiSDKConsole;

public class DeviceConsoleExtension implements ConsoleExtension {

	@Override
    public void init(RuyiSDKConsole console) {
//        console.logInfo("Device extension loaded");
        console.logInfo("[Devices Plugin] Console extension initialized");
    }
}
