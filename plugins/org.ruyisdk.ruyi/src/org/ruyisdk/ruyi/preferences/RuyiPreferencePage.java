package org.ruyisdk.ruyi.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.ruyisdk.ruyi.Activator;

public class RuyiPreferencePage extends FieldEditorPreferencePage 
        implements IWorkbenchPreferencePage {

    public RuyiPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Configure Ruyi SDK settings");
    }

    @Override
    public void init(IWorkbench workbench) {}

    @Override
    protected void createFieldEditors() {
        // 安装路径部分
        Composite pathComposite = new Composite(getFieldEditorParent(), SWT.NONE);
        pathComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        StringFieldEditor pathEditor = new StringFieldEditor(
            RuyiPreferenceConstants.P_INSTALL_PATH, 
            "Installation Path:", 
            pathComposite);
        addField(pathEditor);
        
        Button browseButton = new Button(pathComposite, SWT.PUSH);
        browseButton.setText("Browse...");
        browseButton.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                DirectoryDialog dialog = new DirectoryDialog(getShell());
                String path = dialog.open();
                if (path != null) {
                    pathEditor.setStringValue(path);
                }
            }
        });

        // 自动检查更新
        addField(new BooleanFieldEditor(
            RuyiPreferenceConstants.P_CHECK_ON_STARTUP,
            "Check for updates on startup", 
            getFieldEditorParent()));

        // 跳过版本检查
        addField(new BooleanFieldEditor(
            RuyiPreferenceConstants.P_SKIP_VERSION_CHECK,
            "Skip version check notifications", 
            getFieldEditorParent()));

        // 镜像源选择
        String[][] mirrors = {
            {"Official Repository", "official"},
            {"Mirror Repository (ISCAS)", "mirror"},
            {"Custom...", "custom"}
        };
        ComboFieldEditor mirrorEditor = new ComboFieldEditor(
            RuyiPreferenceConstants.P_REPOSITORY,
            "Repository Source:", 
            mirrors, 
            getFieldEditorParent());
        addField(mirrorEditor);

        // 遥测设置
        addField(new RadioGroupFieldEditor(
            RuyiPreferenceConstants.P_TELEMETRY,
            "Telemetry Settings:", 
            1,
            new String[][] {
                {"Enabled (recommended)", "on"},
                {"Anonymous only", "anonymous"},
                {"Disabled", "off"}
            },
            getFieldEditorParent()));

        // 版本信息
        Label versionLabel = new Label(getFieldEditorParent(), SWT.NONE);
        versionLabel.setText("Current Version: " + getPreferenceStore().getString(RuyiPreferenceConstants.P_CURRENT_VERSION));
    }

    @Override
    public boolean performOk() {
        boolean result = super.performOk();
        if (result) {
            // 保存后可能需要执行的操作
        }
        return result;
    }
}