package org.ruyisdk.ruyi.preferences;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.eclipse.jface.preference.*;
import org.eclipse.swt.*;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PlatformUI;

/**
 * 增强版目录选择字段编辑器
 */
public class DirectoryFieldEditor extends StringFieldEditor {
	private Button browseButton;
//    private Composite parent;
	private String initialPath;

//    public static DirectoryFieldEditor create(String name, String labelText, String initPath, Composite parent) {
//        // Do any pre-processing here
//        DirectoryFieldEditor editor = new DirectoryFieldEditor(name, labelText, parent);
//        editor.parent = parent;
//        editor.defaultPath = initPath;
//        return editor;
//    }
//    
//    private DirectoryFieldEditor(String name, String labelText, Composite parent) {
//        super(name, labelText, parent);
//    }

	public DirectoryFieldEditor(String name, String labelText, String installPath, Composite parent) {
		// TODO Auto-generated constructor stub
		super(name, labelText, parent);
		this.initialPath = installPath;
		System.out.println("DirectoryFieldEditor initial path: " + installPath);
		
		// 确保父类初始化完成后再设置值
        if (getTextControl() != null) {
            getTextControl().setText(installPath != null ? installPath : "");
        }
	}
	
	@Override
	protected void doLoad() {
	    super.doLoad();
	    if (getStringValue().isEmpty() && initialPath != null) {
	        setStringValue(initialPath);
	    }
	}
	
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		super.doFillIntoGrid(parent, numColumns - 1); // 为按钮预留1列

		// Set initial value from constructor parameter
//		if (initialPath != null) {
//			setStringValue(initialPath);
//		}
		browseButton = new Button(parent, SWT.PUSH);
		browseButton.setText("Browse...");
		browseButton.setFont(parent.getFont());
		browseButton.addListener(SWT.Selection, event -> browse());
		
		GridData gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		browseButton.setLayoutData(gd);
	}
    
	private void browse() {
		DirectoryDialog dialog = new DirectoryDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell());
		dialog.setMessage("Select installation directory");
		dialog.setFilterPath(getTextControl().getText());

		// Use current value as filter path, fall back to initial path if empty
		String currentPath = getStringValue();
		dialog.setFilterPath(currentPath.isEmpty() ? initialPath : currentPath);

		String path = dialog.open();
		if (path != null) {
			setStringValue(path);
		}
	}

	@Override
	protected boolean doCheckState() {
		String path = getTextControl().getText();
		if (path.isEmpty()) {
			setErrorMessage("Directory cannot be empty");
			return false;
		}

		Path dir = Paths.get(path);
		if (!Files.exists(dir)) {
			setErrorMessage("Directory does not exist");
			return false;
		}

		if (!Files.isWritable(dir)) {
			setErrorMessage("Directory is not writable");
			return false;
		}

		setErrorMessage(null);
		return true;
	}

	@Override
	public int getNumberOfControls() {
		return 3; // 标签+文本框+按钮
	}
}
