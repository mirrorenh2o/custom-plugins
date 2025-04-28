package org.ruyisdk.ruyi.ui.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class RuyiErrorDialog extends Dialog {
    private final String title;
    private final String message;
    private final String details;
    
    public RuyiErrorDialog(Shell parentShell, String title, String message, String details) {
        super(parentShell);
        this.title = title;
        this.message = message;
        this.details = details;
    }
    
    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        Composite container = (Composite) super.createDialogArea(parent);
        GridLayout layout = new GridLayout(1, false);
        layout.marginHeight = 10;
        layout.marginWidth = 10;
        container.setLayout(layout);
        
        // 错误消息
        Label messageLabel = new Label(container, SWT.WRAP);
        messageLabel.setText(message);
        GridData messageData = new GridData(SWT.FILL, SWT.TOP, true, false);
        messageData.widthHint = 400;
        messageLabel.setLayoutData(messageData);
        
        // 详细信息区域
        if (details != null && !details.isEmpty()) {
            Group detailsGroup = new Group(container, SWT.NONE);
            detailsGroup.setText("Details");
            detailsGroup.setLayout(new GridLayout());
            detailsGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            
            StyledText detailsText = new StyledText(detailsGroup, 
                SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL | SWT.READ_ONLY);
            detailsText.setText(details);
            GridData textData = new GridData(SWT.FILL, SWT.FILL, true, true);
            textData.heightHint = 150;
            detailsText.setLayoutData(textData);
        }
        
        return container;
    }
    
    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
    }
    
    public static void openError(String title, String message, String details) {
        new RuyiErrorDialog(
            Display.getDefault().getActiveShell(),
            title,
            message,
            details
        ).open();
    }
}