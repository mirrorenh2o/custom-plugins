package org.ruyisdk.devices.views;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.ruyisdk.devices.model.Device;

public class DeviceDialog extends TitleAreaDialog {
    private Device device;
    private String titleText;
    private Text nameText;
    private Text chipText;
    private Text vendorText;
    private Text versionText;
    
    public DeviceDialog(Shell parentShell, Device device) {
        super(parentShell);
        this.device = device;
    }
    
//    @Override
//    protected void configureShell(Shell shell) {
//        super.configureShell(shell);
//        shell.setText(device.getName() == null ? "Add New Device" : "Edit Device");
//        this.titleText = "Add New Device";
//    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
    	setTitle("Add New Device");
        Composite area = (Composite) super.createDialogArea(parent);
        Composite container = new Composite(area, SWT.NONE);
        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        new Label(container, SWT.NONE).setText("Name:");
        nameText = new Text(container, SWT.BORDER);
        nameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(container, SWT.NONE).setText("Chip:");
        chipText = new Text(container, SWT.BORDER);
        chipText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(container, SWT.NONE).setText("Vendor:");
        vendorText = new Text(container, SWT.BORDER);
        vendorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        new Label(container, SWT.NONE).setText("Version:");
        versionText = new Text(container, SWT.BORDER);
        versionText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        if (device != null) {
            nameText.setText(device.getName());
            chipText.setText(device.getChip());
            vendorText.setText(device.getVendor());
            versionText.setText(device.getVersion());
        }

        return area;
    }
    
    
    @Override
    protected void okPressed() {
    	device = new Device(
            nameText.getText(),
            chipText.getText(),
            vendorText.getText(),
            versionText.getText(),
            false
        );
        super.okPressed();
    }

    public Device getDevice() {
        return device;
    }
}