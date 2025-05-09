package org.ruyisdk.ruyi.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.preferences.RuyiConfigPreferencePage;
import org.ruyisdk.ruyi.util.RuyiLogger;

/**
 * 处理"打开首选项"命令
 */
public class OpenPreferencesHandler extends AbstractHandler {
    private static final RuyiLogger logger = Activator.getDefault().getLogger();

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        try {
            Shell shell = HandlerUtil.getActiveShell(event);
            PreferenceManager pm = new PreferenceManager();
            
            pm.addToRoot(new PreferenceNode("ruyi", "Ruyi SDK", 
                null, RuyiConfigPreferencePage.class.getName()));
            
            PreferenceDialog dialog = new PreferenceDialog(shell, pm);
            dialog.create();
            dialog.setMessage("Ruyi SDK Preferences");
            dialog.open();
            
            return null;
        } catch (Exception e) {
            logger.logError("Failed to open preferences", e);
            throw new ExecutionException("Open preferences failed", e);
        }
    }
    
    public static void openRuyiPreferencePage(Shell parentShell) {
        PreferenceDialog dialog = PreferencesUtil.createPreferenceDialogOn(
            parentShell,
            "org.ruyisdk.preferences.core", // 必须与plugin.xml中的ID一致
            null, // 其他要显示的页面ID（null表示只显示当前页）
            null  // 初始选择数据
        );
        dialog.open();
    }
}