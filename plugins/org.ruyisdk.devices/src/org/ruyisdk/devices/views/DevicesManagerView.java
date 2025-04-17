package org.ruyisdk.devices.views;

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.viewers.*;
import org.ruyisdk.devices.model.Device;
import org.ruyisdk.devices.services.DeviceService;
import org.ruyisdk.devices.providers.DeviceLabelProvider;

public class DevicesManagerView extends ViewPart {
    public static final String ID = "org.ruyisdk.devices.views.DevicesView";
    
    private TableViewer tableViewer;
	private Button addButton, editButton, deleteButton, setDefaultButton, saveButton, closeButton;
	private Label infoLabel;
	private DeviceService deviceService = new DeviceService();
	private List<Device> devices;

	@Override
	public void createPartControl(Composite parent) {
		// 设置父容器的布局为 2 列
		parent.setLayout(new GridLayout(2, false));

		// 提示信息
		infoLabel = new Label(parent, SWT.NONE);
		infoLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// 左侧区域：表格
		Composite tableComposite = new Composite(parent, SWT.NONE);
		tableComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		tableComposite.setLayout(new GridLayout(1, false));

		// 创建表格
		tableViewer = new TableViewer(tableComposite, SWT.BORDER | SWT.FULL_SELECTION);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		tableViewer.getTable().setHeaderVisible(true);
		tableViewer.getTable().setLinesVisible(true);

		// 添加表头
		String[] columnNames = { "开发板型号", "SOC", "厂商", "版本", "状态" };
		for (String columnName : columnNames) {
			TableColumn column = new TableColumn(tableViewer.getTable(), SWT.NONE);
			column.setText(columnName);
			column.setWidth(100);
		}

		// 设置内容提供器和标签提供器
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setLabelProvider(new DeviceLabelProvider());

		// 右侧区域：按钮（纵向排列）
		Composite buttonComposite = new Composite(parent, SWT.NONE);
		buttonComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
		buttonComposite.setLayout(new GridLayout(1, false));

		// 添加按钮
		addButton = new Button(buttonComposite, SWT.PUSH);
		addButton.setText("Add");
		addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		editButton = new Button(buttonComposite, SWT.PUSH);
		editButton.setText("Edit");
		editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		editButton.setEnabled(false);

		deleteButton = new Button(buttonComposite, SWT.PUSH);
		deleteButton.setText("Delete");
		deleteButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		deleteButton.setEnabled(false);

		setDefaultButton = new Button(buttonComposite, SWT.PUSH);
		setDefaultButton.setText("Set Default");
		setDefaultButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		setDefaultButton.setEnabled(false);

		// 底部区域：Save 和 Close 按钮（横向排列）
		Composite bottomComposite = new Composite(parent, SWT.NONE);
		bottomComposite.setLayoutData(new GridData(SWT.FILL, SWT.BOTTOM, true, false, 2, 1));
		bottomComposite.setLayout(new GridLayout(2, true));

		saveButton = new Button(bottomComposite, SWT.PUSH);
		saveButton.setText("Save");
		saveButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		closeButton = new Button(bottomComposite, SWT.PUSH);
		closeButton.setText("Close");
		closeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		// 加载数据
		refreshView();

		// 初始化表格数据
		refreshTable();

		// 绑定按钮事件
		bindButtonEvents();
	}

	@Override
	public void setFocus() {
		tableViewer.getControl().setFocus();
	}

	private void refreshView() {
		devices = deviceService.getDevices();
		tableViewer.setInput(devices);

		boolean hasDevices = !devices.isEmpty();
		boolean hasDefault = devices.stream().anyMatch(Device::isDefault);

		editButton.setEnabled(hasDevices);
		deleteButton.setEnabled(hasDevices);
		setDefaultButton.setEnabled(hasDevices);

		if (!hasDevices) {
			infoLabel.setText("您还未添加任何RISC-V开发板。用户添加开发板");
		} else if (!hasDefault) {
			infoLabel.setText("您还未设置默认开发板，请设置一款开发板为默认开发板。设置后IDE将按照开发板型号为您推荐合适的开发资源。");
		} else {
			infoLabel.setText("您的设备信息如下");
		}
	}

	/**
	 * 刷新表格数据
	 */
	private void refreshTable() {
		tableViewer.setInput(deviceService.getDevices());
		for (TableColumn column : tableViewer.getTable().getColumns()) {
			column.pack();
		}
	}

	/**
	 * 绑定按钮事件
	 */
	private void bindButtonEvents() {
		addButton.addListener(SWT.Selection, e -> {
			DeviceDialog dialog = new DeviceDialog(getSite().getShell(), null);
			if (dialog.open() == Window.OK) {
				deviceService.addDevice(dialog.getDevice());
				refreshTable();
			}
		});

		editButton.addListener(SWT.Selection, e -> {
			Device selectedDevice = getSelectedDevice();
			if (selectedDevice != null) {
				DeviceDialog dialog = new DeviceDialog(getSite().getShell(), selectedDevice);
				if (dialog.open() == Window.OK) {
					deviceService.updateDevice(selectedDevice, dialog.getDevice());
					refreshTable();
				}
			}
		});

		deleteButton.addListener(SWT.Selection, e -> {
			Device selectedDevice = getSelectedDevice();
			if (selectedDevice != null) {
				deviceService.deleteDevice(selectedDevice);
				refreshTable();
			}
		});

		setDefaultButton.addListener(SWT.Selection, e -> {
			Device selectedDevice = getSelectedDevice();
			if (selectedDevice != null) {
				deviceService.setDefaultDevice(selectedDevice);
				refreshTable();
			}
		});

		saveButton.addListener(SWT.Selection, e -> {
			deviceService.saveDevices();
			refreshTable();
			refreshView();
		});

		closeButton.addListener(SWT.Selection, e -> {
			getSite().getPage().hideView(this);
		});
	}

	/**
	 * 获取选中的开发板
	 */
	private Device getSelectedDevice() {
		IStructuredSelection selection = (IStructuredSelection) tableViewer.getSelection();
		return (Device) selection.getFirstElement();
	}
}