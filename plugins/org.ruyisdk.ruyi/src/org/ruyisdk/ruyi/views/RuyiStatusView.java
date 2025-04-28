package org.ruyisdk.ruyi.views;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.core.RuyiCore;
import org.ruyisdk.ruyi.core.RuyiVersionChecker;
import org.ruyisdk.ruyi.util.RuyiLogger;

public class RuyiStatusView extends ViewPart {
    public static final String ID = "org.ruyisdk.ruyi.views.status";
    
    private final RuyiLogger logger = Activator.getDefault().getLogger();
    private TableViewer viewer;
    private Label statusLabel;
    
    // 状态项数据模型
    public static class StatusItem {
        String property;
        String value;
        Image icon;
        
        public StatusItem(String property, String value, Image icon) {
            this.property = property;
            this.value = value;
            this.icon = icon;
        }
    }

    @Override
    public void createPartControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        
        // 状态标签
        statusLabel = new Label(container, SWT.NONE);
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
        statusLabel.setText("Initializing Ruyi SDK status...");
        
        // 状态表格
        viewer = new TableViewer(container, SWT.BORDER | SWT.FULL_SELECTION);
        viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        
        // 创建表格列
        TableViewerColumn propColumn = new TableViewerColumn(viewer, SWT.NONE);
        propColumn.getColumn().setWidth(150);
        propColumn.getColumn().setText("Property");
        propColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((StatusItem)element).property;
            }
            @Override
            public Image getImage(Object element) {
                return ((StatusItem)element).icon;
            }
        });
        
        TableViewerColumn valueColumn = new TableViewerColumn(viewer, SWT.NONE);
        valueColumn.getColumn().setWidth(250);
        valueColumn.getColumn().setText("Value");
        valueColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((StatusItem)element).value;
            }
        });
        
        viewer.setContentProvider(ArrayContentProvider.getInstance());
        viewer.getTable().setHeaderVisible(true);
        viewer.getTable().setLinesVisible(true);
        
        // 刷新按钮
        Button refreshBtn = new Button(container, SWT.PUSH);
        refreshBtn.setText("Refresh");
        refreshBtn.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
        refreshBtn.addListener(SWT.Selection, e -> refreshStatus());
        
        // 初始刷新
        refreshStatus();
    }

    private void refreshStatus() {
        try {
            RuyiCore core = Activator.getDefault().getRuyiCore();
            
            StatusItem[] items = {
                new StatusItem("Installation Path", 
                    core.getInstallManager().getInstallPath(), 
                    getStatusImage("folder")),
                
                new StatusItem("Installed Version", 
                    getVersionSafe(core.getVersionChecker()), 
                    getStatusImage("version")),
                
                new StatusItem("Latest Version", 
                    getLatestVersionSafe(core.getVersionChecker()), 
                    getStatusImage("update")),
                
                new StatusItem("Repository", 
                    core.getInstallManager().getRepositoryUrl(), 
                    getStatusImage("repo")),
                
                new StatusItem("Telemetry", 
                    core.getInstallManager().isTelemetryEnabled() ? "Enabled" : "Disabled", 
                    getStatusImage("telemetry"))
            };
            
            viewer.setInput(items);
            statusLabel.setText("Last updated: " + new java.util.Date());
            
        } catch (Exception e) {
            logger.logError("Failed to refresh Ruyi status", e);
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    // 安全获取本地版本
    private String getVersionSafe(RuyiVersionChecker versionChecker) {
        try {
            String version = versionChecker.getLatestVersion(null);
            return version != null ? version : "Not installed";
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    // 安全获取最新版本
    private String getLatestVersionSafe(RuyiVersionChecker versionChecker) {
        try {
            return versionChecker.getLatestVersion(null);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private Image getStatusImage(String type) {
        // 从插件图标注册表获取图片
        return Activator.getDefault().getImageRegistry().get(type);
    }

    @Override
    public void setFocus() {
        viewer.getControl().setFocus();
    }
}