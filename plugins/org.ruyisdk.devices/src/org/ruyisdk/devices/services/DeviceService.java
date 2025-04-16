package org.ruyisdk.devices.services;

import java.util.ArrayList;
import java.util.List;
import org.ruyisdk.devices.model.Device;

public class DeviceService {
	private List<Device> devices = new ArrayList<>();
    private PropertiesService propertiesService = new PropertiesService();

    public DeviceService() {
    	devices = propertiesService.loadDevices();
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void addDevice(Device device) {
    	devices.add(device);
    }

    public void updateDevice(Device oldDevice, Device newDevice) {
        int index = devices.indexOf(oldDevice);
        if (index != -1) {
        	devices.set(index, newDevice);
        }
    }

    public void deleteDevice(Device device) {
    	devices.remove(device);
    }

    public void setDefaultDevice(Device device) {
        for (Device b : devices) {
            b.setDefault(false);
        }
        device.setDefault(true);
    }

    public void saveDevices() {
        propertiesService.saveDevices(devices);
    }
}
