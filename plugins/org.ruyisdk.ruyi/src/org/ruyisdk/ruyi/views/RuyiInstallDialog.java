package org.ruyisdk.ruyi.views;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.ruyisdk.core.config.Constants;

public class RuyiInstallDialog extends TitleAreaDialog {
    
    private Text pathText;
    private String installPath;
    
    public RuyiInstallDialog(Shell parentShell) {
        super(parentShell);
    }
    
    @Override
    protected Control createDialogArea(Composite parent) {
        setTitle("安装 Ruyi SDK");
        setMessage("请指定 Ruyi 的安装路径", IMessageProvider.INFORMATION);
        
        Composite container = (Composite) super.createDialogArea(parent);
        Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        composite.setLayout(new GridLayout(2, false));
        
        new Label(composite, SWT.NONE).setText("安装路径:");
        pathText = new Text(composite, SWT.BORDER);
        pathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        pathText.setText(Constants.Ruyi.INSTALL_PATH);
        
        Button browseButton = new Button(composite, SWT.PUSH);
        browseButton.setText("浏览...");
        browseButton.addListener(SWT.Selection, event -> {
            DirectoryDialog dialog = new DirectoryDialog(getShell());
            dialog.setFilterPath(pathText.getText());
            String selected = dialog.open();
            if (selected != null) {
                pathText.setText(selected);
            }
        });
        
        return container;
    }
    
    @Override
    protected void okPressed() {
        installPath = pathText.getText();
        Constants.Ruyi.INSTALL_PATH = installPath;
        super.okPressed();
    }
    
    public String getInstallPath() {
        return installPath;
    }
}
