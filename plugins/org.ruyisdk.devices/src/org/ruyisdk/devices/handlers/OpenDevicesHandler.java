package org.ruyisdk.devices.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.handlers.HandlerUtil;
import org.ruyisdk.devices.views.DevicesManagerView;

public class OpenDevicesHandler extends AbstractHandler {
	
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        // 获取当前工作台的页面
        IWorkbenchPage page = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
        if (page == null) {
            throw new ExecutionException("No active workbench page found.");
        }

        try {           
         // 尝试查找已打开的 DevicesManagerView 视图
            IViewPart viewPart = page.findView(DevicesManagerView.ID);
            if (viewPart == null) {
                // 如果视图未打开，则显示它
                page.showView(DevicesManagerView.ID);
            } else {
            	// 如果视图已经打开，则将其带到最前面
                page.bringToTop(viewPart);
//                // 如果视图已经打开，则将其激活
//                page.activate(viewPart);
            }
        } catch (PartInitException e) {
            // 如果视图打开失败，抛出异常
            throw new ExecutionException("Failed to open RISC-V Device Manager View", e);
            
        }

        return null; // 返回 null 表示命令执行成功
    }
}