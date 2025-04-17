package org.ruyisdk.devices;

import org.eclipse.jface.preference.*;
import org.eclipse.jface.viewers.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.ruyisdk.devices.model.Device;
import org.ruyisdk.devices.views.DeviceDialog;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.*;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import org.eclipse.jface.window.Window;

public class DevicePreferencePage2 extends FieldEditorPreferencePage 
        implements IWorkbenchPreferencePage {

    private TableViewer tableViewer;
    private List<Device> devices = new ArrayList<>();
    private Button addButton;
    private Button removeButton;
    private Button editButton;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public DevicePreferencePage2() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("Manage connected devices for RuyiSDK");
    }

    @Override
    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        parent.setLayout(new GridLayout(2, false));

        // Device table
        tableViewer = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
        Table table = tableViewer.getTable();
        table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 4));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);

        // Table columns
        TableViewerColumn nameColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        nameColumn.getColumn().setText("Name");
        nameColumn.getColumn().setWidth(150);
        nameColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Device)element).getName();
            }
        });

        TableViewerColumn chipColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        chipColumn.getColumn().setText("Chip");
        chipColumn.getColumn().setWidth(100);
        chipColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Device)element).getChip();
            }
        });

        TableViewerColumn vendorColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        vendorColumn.getColumn().setText("Vendor");
        vendorColumn.getColumn().setWidth(100);
        vendorColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Device)element).getVendor();
            }
        });
        
        TableViewerColumn versionColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        versionColumn.getColumn().setText("Veision");
        versionColumn.getColumn().setWidth(60);
        versionColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Device)element).getVersion();
            }
        });
        
        TableViewerColumn defaultColumn = new TableViewerColumn(tableViewer, SWT.NONE);
        defaultColumn.getColumn().setText("isDefault");
        defaultColumn.getColumn().setWidth(100);
        defaultColumn.setLabelProvider(new ColumnLabelProvider() {
            @Override
            public String getText(Object element) {
                return ((Device)element).isDefault()==true? "True":"";
            }
        });

        // Button panel
        Composite buttonPanel = new Composite(parent, SWT.NONE);
        buttonPanel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, false, false));
        buttonPanel.setLayout(new GridLayout(1, false));

        addButton = new Button(buttonPanel, SWT.PUSH);
        addButton.setText("Add");
        addButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        editButton = new Button(buttonPanel, SWT.PUSH);
        editButton.setText("Edit");
        editButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        removeButton = new Button(buttonPanel, SWT.PUSH);
        removeButton.setText("Remove");
        removeButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

        // Load devices
        loadDevices();
        
        // Setup listeners
        setupListeners();
    }

    private void loadDevices() {
        String json = DevicePreferences.getDevicesJson();
//        devices = parseDevicesFromJson(json);
//        tableViewer.setInput(devices);
//        tableViewer.refresh();
        Type listType = new TypeToken<ArrayList<Device>>(){}.getType();
        devices = gson.fromJson(json, listType);
        if (devices == null) {
            devices = new ArrayList<>();
        }
        tableViewer.setInput(devices);
        tableViewer.refresh();
    }

//    private List<Device> parseDevicesFromJson(String json) {
//        List<Device> result = new ArrayList<>();
//        try {
//            JsonArray array = new JsonArray(json);
//            for (int i = 0; i < array.length(); i++) {
//                JsonObject obj = array.getJsonObject(i);
//                Device device = new Device(
//                    obj.getString("id"),
//                    obj.getString("name"),
//                    obj.getString("type")
//                );
//                result.add(device);
//            }
//        } catch (Exception e) {
//            Activator.logError("Failed to parse devices", e);
//        }
//        return result;
//    }

    private void saveDevices() {
//        JsonArray array = new JsonArray();
//        for (Device device : devices) {
//            JsonObject obj = new JsonObject();
//            obj.put("id", device.getId());
//            obj.put("name", device.getName());
//            obj.put("type", device.getType());
//            array.put(obj);
//        }
//        DevicePreferences.saveDevicesJson(array.toString());
        
        String json = gson.toJson(devices);
        DevicePreferences.saveDevicesJson(json);
    }

    private void setupListeners() {
        addButton.addListener(SWT.Selection, e -> {
            DeviceDialog dialog = new DeviceDialog(PlatformUI.getWorkbench()
                .getActiveWorkbenchWindow().getShell(), null);
            if (dialog.open() == Window.OK) {
                devices.add(dialog.getDevice());
                tableViewer.refresh();
                saveDevices();
            }
        });

        editButton.addListener(SWT.Selection, e -> {
            IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
            if (!selection.isEmpty()) {
                Device device = (Device)selection.getFirstElement();
                DeviceDialog dialog = new DeviceDialog(
                    PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), 
                    device);
                if (dialog.open() == Window.OK) {
                    tableViewer.refresh();
                    saveDevices();
                }
            }
        });

        removeButton.addListener(SWT.Selection, e -> {
            IStructuredSelection selection = (IStructuredSelection)tableViewer.getSelection();
            if (!selection.isEmpty()) {
                devices.removeAll(selection.toList());
                tableViewer.refresh();
                saveDevices();
            }
        });
    }

    @Override
    public boolean performOk() {
        saveDevices();
        return super.performOk();
    }
}