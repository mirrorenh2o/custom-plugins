package org.ruyisdk.ruyi.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class RuyiUpdateDialog extends Dialog {
    private final String currentVersion;
    private final String newVersion;
    private Button skipCheckbox;
    
    public RuyiUpdateDialog(Shell parentShell, String currentVersion, String newVersion) {
        super(parentShell);
        this.currentVersion = currentVersion;
        this.newVersion = newVersion;
    }
    
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Ruyi SDK Update Available");
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        container.setLayout(layout);
        
        Label messageLabel = new Label(container, SWT.WRAP);
        messageLabel.setText(String.format(
            "A new version of Ruyi SDK is available:\n\n" +
            "Current version: %s\n" +
            "New version: %s\n\n" +
            "Would you like to update now?", 
            currentVersion, newVersion));
        
        GridData messageData = new GridData(SWT.FILL, SWT.TOP, true, false);
        messageData.widthHint = 400;
        messageLabel.setLayoutData(messageData);
        
        skipCheckbox = new Button(container, SWT.CHECK);
        skipCheckbox.setText("Skip this version and don't remind me again");
        
        return container;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "Update Now", true);
        createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
    }
    
    @Override
    protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            // 触发更新逻辑
        } else {
            if (skipCheckbox.getSelection()) {
                // 保存跳过此版本的设置
            }
        }
        super.buttonPressed(buttonId);
    }
    
    public boolean shouldSkipFutureNotifications() {
        return skipCheckbox.getSelection();
    }
}