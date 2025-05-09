package org.ruyisdk.ruyi.preferences;

import java.io.IOException;
import java.util.Collections;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.ruyisdk.ruyi.Activator;

public class RuyiConfigPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	private RuyiInstallPathPreference installPreference;
	private RepoConfigPreference repoPreference;
	private TelemetryPreference telemetryPreference;

	@Override
	public void init(IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
//        setDescription("Configure Ruyi settings");
//      noDefaultAndApplyButton();
//		noDefaultButton();
	}

//    @Override
//    protected void createFieldEditors() {
//        Composite parent = getFieldEditorParent();
//        
//        // 确保父容器使用GridLayout
//        if (!(parent.getLayout() instanceof GridLayout)) {
////            parent.setLayout(new GridLayout(1, false));
//            parent.setLayout(new FillLayout(SWT.VERTICAL));
//        }
//        
//        // 1. 安装路径配置
//        addField(new DirectoryFieldEditor(
//		            "RUYI_INSTALL_PATH", 
//		            "Installation Directory:", 
//		            getInstallPath(),
//		            parent
//        		)
//        );
//
//        // 2. 仓库配置
//        addField(new CustomCompositeFieldEditor("REPO_SECTION",parent) {
//            @Override
//            protected void createControl(Composite parent) {
//                new RepoConfigPreferenceSection(parent).createSection();
//            }
//        });
//
//        // 3. 遥测配置
//        addField(new CustomCompositeFieldEditor("TELEMETRY_SECTION",parent) {
//            @Override
//            protected void createControl(Composite parent) {
//                new TelemetryPreferenceSection(parent).createSection();
//            }
//        });
//        
//        // 强制布局刷新
//        parent.layout(true, true);
//        printLayoutHierarchy(parent, 0);
//    }

	@Override
	protected void createFieldEditors() {
		// 1. 获取父容器并强制设置为垂直布局
		Composite parent = getFieldEditorParent();
		parent.setLayout(new GridLayout(1, false));

		// 2. 创建垂直容器作为所有内容的父级
		Composite content = new Composite(parent, SWT.NONE);
		content.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		content.setLayout(new GridLayout(1, false));
//		content.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_GREEN));

		// 3. 手动创建安装路径编辑器
//		createDirectoryEditor(content);
		installPreference = new RuyiInstallPathPreference(content);
		installPreference.createSection();

		// 4. 仓库配置
		repoPreference = new RepoConfigPreference(content);
		repoPreference.createSection();

		// 5. 遥测配置
		telemetryPreference = new TelemetryPreference(content);
		telemetryPreference.createSection();

		// 布局调试
//		printControlHierarchy(content, 0);

	}

	private void printControlHierarchy(Composite comp, int level) {
		String indent = String.join("", Collections.nCopies(level, "  "));
		System.out.println(indent + comp + " " + comp.getLayout());
		for (Control c : comp.getChildren()) {
			System.out.println(indent + " |- " + c + " " + c.getLayoutData());
			if (c instanceof Composite) {
				printControlHierarchy((Composite) c, level + 1);
			}
		}
	}

	// 1. 恢复默认值逻辑
	@Override
	protected void performDefaults() {
		super.performDefaults(); // 先调用父类默认处理

		// UI
		installPreference.defaultedInstallPath();
		repoPreference.defaultedRepoState();
		telemetryPreference.defaultedTelemetryState();

		MessageDialog.openInformation(getShell(), "Defaults Restored",
				"All settings have been reset to default values");
	}

	// 2. 应用按钮逻辑
	@Override
	public boolean performOk() {
		boolean result = super.performOk(); // 先执行默认存储

		// 自定义确认逻辑
		if (result) {
			applySettingsToRuntime();
		}
		return result;
	}

	// 3. 单独的应用逻辑（当直接点击Apply时调用）
	@Override
	protected void performApply() {
		super.performApply();

		// 立即应用设置
		applySettingsToRuntime();
//        StatusUtil.showInfo("Settings applied successfully");
		
		// 用户保存配置后触发快速检查
//	    CheckResult result = new QuickCheckJob().runCheck();
//	    if (result.needAction()) {
//	        RuyiInstallWizard.openBasedOnResult(result);
//	    }
	}

	private void applySettingsToRuntime() {
		try {
			installPreference.saveInstallPath();
			repoPreference.saveRepoConfigs();
			telemetryPreference.saveTelemetryConfigs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//        // 实际应用设置的逻辑示例：
//        String installPath = getPreferenceStore().getString("RUYI_INSTALL_PATH");
//        boolean telemetryEnabled = getPreferenceStore().getBoolean("ENABLE_TELEMETRY");
//        
//        // 更新运行时配置
//        ConfigManager.applyNewSettings(installPath, telemetryEnabled);
	}

}
