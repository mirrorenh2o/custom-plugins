package org.ruyisdk.ruyi.ui.dialogs;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

public class RepositorySelector {
    private Composite parent;
    private Button[] repoCheckboxes;
    private Text customUrlText;
    private String[] repoUrls;
    private String[] repoDisplayNames;
    
    // 定义仓库优先级顺序 (数组索引越小优先级越高)
    private int[] priorityOrder = {2, 1, 0}; // 默认: Custom > GitHub > China Mirror
    
    public RepositorySelector(Composite parent) {
        this.parent = parent;
        initializeRepositories();
        createUI();
    }
    
    private void initializeRepositories() {
        repoDisplayNames = new String[] {
            "China ISCAS Mirror",
            "GitHub Repo",
            "Custom Repository"
        };
        
        repoUrls = new String[] {
            "https://mirror.iscas.ac.cn/git/ruyisdk/packages-index.git",
            "https://github.com/ruyisdk/packages-index.git",
            "" // Custom URL将由用户输入
        };
    }
    
    private void createUI() {
        Composite container = new Composite(parent, SWT.NONE);
        container.setLayout(new GridLayout(1, false));
        container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        
        Label titleLabel = new Label(container, SWT.NONE);
        titleLabel.setText("Package Index Repositories (Higher priority first):");
        
        // 创建优先级顺序设置
        createPrioritySetting(container);
        
        // 创建仓库选择复选框
        createRepositoryCheckboxes(container);
        
        // 创建自定义URL输入
        createCustomUrlInput(container);
    }
    
    private void createPrioritySetting(Composite parent) {
        Group priorityGroup = new Group(parent, SWT.NONE);
        priorityGroup.setText("Repository Priority Order");
        priorityGroup.setLayout(new GridLayout(3, false));
        priorityGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label infoLabel = new Label(priorityGroup, SWT.NONE);
        infoLabel.setText("Drag to reorder (First has highest priority):");
        infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        // 创建可拖动的优先级列表
        Table priorityTable = new Table(priorityGroup, SWT.BORDER | SWT.SINGLE);
        priorityTable.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 3, 1));
        
        TableColumn column = new TableColumn(priorityTable, SWT.NONE);
        column.setWidth(200);
        
        for (int i = 0; i < priorityOrder.length; i++) {
            TableItem item = new TableItem(priorityTable, SWT.NONE);
            item.setText(repoDisplayNames[priorityOrder[i]]);
        }
        
        // 添加拖动排序功能
        addDragSupport(priorityTable);
    }
    
    private void addDragSupport(Table table) {
        Transfer[] types = new Transfer[] {TextTransfer.getInstance()};
        
        DragSource source = new DragSource(table, DND.DROP_MOVE);
        source.setTransfer(types);
        source.addDragListener(new DragSourceAdapter() {
            public void dragSetData(DragSourceEvent event) {
                event.data = String.valueOf(table.getSelectionIndex());
            }
        });
        
        DropTarget target = new DropTarget(table, DND.DROP_MOVE);
        target.setTransfer(types);
        target.addDropListener(new DropTargetAdapter() {
            public void drop(DropTargetEvent event) {
                if (event.data == null) return;
                
                int sourceIndex = Integer.parseInt(event.data.toString());
                TableItem item = table.getItem(table.toControl(new Point(event.x, event.y)));
                int targetIndex = item == null ? table.getItemCount() - 1 : table.indexOf(item);
                
                if (sourceIndex != targetIndex) {
                    // 更新优先级顺序
                    int temp = priorityOrder[sourceIndex];
                    if (sourceIndex < targetIndex) {
                        System.arraycopy(priorityOrder, sourceIndex + 1, priorityOrder, sourceIndex, targetIndex - sourceIndex);
                    } else {
                        System.arraycopy(priorityOrder, targetIndex, priorityOrder, targetIndex + 1, sourceIndex - targetIndex);
                    }
                    priorityOrder[targetIndex] = temp;
                    
                    // 刷新显示
                    table.removeAll();
                    for (int i = 0; i < priorityOrder.length; i++) {
                        TableItem newItem = new TableItem(table, SWT.NONE);
                        newItem.setText(repoDisplayNames[priorityOrder[i]]);
                    }
                }
            }
        });
    }
    
    private void createRepositoryCheckboxes(Composite parent) {
        repoCheckboxes = new Button[repoDisplayNames.length];
        
        for (int i = 0; i < repoDisplayNames.length; i++) {
            repoCheckboxes[i] = new Button(parent, SWT.CHECK);
            repoCheckboxes[i].setText(String.format("%s: %s", 
                repoDisplayNames[i], 
                i == 2 ? "(User specified)" : repoUrls[i]));
            repoCheckboxes[i].setSelection(true); // 默认全部选中
        }
    }
    
    private void createCustomUrlInput(Composite parent) {
        Composite customContainer = new Composite(parent, SWT.NONE);
        customContainer.setLayout(new GridLayout(2, false));
        customContainer.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        Label customLabel = new Label(customContainer, SWT.NONE);
        customLabel.setText("Custom URL:");
        
        customUrlText = new Text(customContainer, SWT.BORDER);
        customUrlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        customUrlText.setEnabled(repoCheckboxes[2].getSelection());
        
        // 绑定自定义仓库复选框状态变化
        repoCheckboxes[2].addListener(SWT.Selection, e -> {
            customUrlText.setEnabled(repoCheckboxes[2].getSelection());
        });
    }
    
    // 获取按优先级排序的选中仓库URL列表
    public List<String> getSelectedRepositories() {
        List<String> selectedUrls = new ArrayList<>();
        
        // 更新自定义URL
        if (repoCheckboxes[2].getSelection()) {
            repoUrls[2] = customUrlText.getText().trim();
        }
        
        // 按照优先级顺序收集选中的仓库
        for (int priority : priorityOrder) {
            if (repoCheckboxes[priority].getSelection() && 
                (!repoUrls[priority].isEmpty() || priority == 2)) {
                selectedUrls.add(repoUrls[priority]);
            }
        }
        
        return selectedUrls;
    }
    
    // 设置优先级顺序 (0:China, 1:GitHub, 2:Custom)
    public void setPriorityOrder(int[] newOrder) {
        if (newOrder != null && newOrder.length == 3) {
            this.priorityOrder = newOrder;
        }
    }
}