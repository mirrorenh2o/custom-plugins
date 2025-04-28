package org.ruyisdk.ruyi.ui.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.*;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.layout.*;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.*;
import org.ruyisdk.core.config.Constants;
import org.ruyisdk.core.ruyi.model.RuyiManager;
import org.ruyisdk.core.ruyi.model.RuyiReleaseInfo;
import org.ruyisdk.core.ruyi.model.RuyiVersion;
import org.ruyisdk.core.ruyi.model.SystemInfo;
import org.ruyisdk.core.ruyi.model.RepoConfig;
import org.ruyisdk.ruyi.Activator;
import org.ruyisdk.ruyi.core.RuyiAPI;
import org.ruyisdk.ruyi.core.RuyiInstallManager;
import org.ruyisdk.ruyi.ui.widgets.InstallProgressComposite;
import org.ruyisdk.ruyi.ui.widgets.VersionCompareComposite;
import org.ruyisdk.ruyi.util.RuyiLogger;

public class RuyiInstallWizard extends Wizard {
    private final RuyiInstallManager installManager;
    private final RuyiLogger logger;
    
    private WelcomePage welcomePage;
    private InstallationCheckPage checkPage;
    private VersionComparisonPage versionPage;
    private ConfigurationPage configPage;
    private InstallationPage installPage;
    private CompletionPage completionPage;

    public RuyiInstallWizard() {
        this(Activator.getDefault().getRuyiCore().getInstallManager(),
        		Activator.getDefault().getLogger());
    }

    public RuyiInstallWizard(RuyiInstallManager installManager, RuyiLogger logger) {
        this.installManager = installManager;
        this.logger = logger;
        setNeedsProgressMonitor(true);
        setWindowTitle("Ruyi Installation Wizard");
    }

    @Override
    public void addPages() {
        welcomePage = new WelcomePage();
        checkPage = new InstallationCheckPage(installManager, logger);
        versionPage = new VersionComparisonPage(installManager, logger);
        configPage = new ConfigurationPage(installManager, logger);
        installPage = new InstallationPage(installManager, logger);
        completionPage = new CompletionPage(installManager, logger);
        
        addPage(welcomePage);
        addPage(checkPage);
        addPage(versionPage);
        addPage(configPage);
        addPage(installPage);
        addPage(completionPage);
    }

    @Override
    public boolean performFinish() {
        return installPage.performFinish();
    }
    
    public void open() {
        WizardDialog dialog = new WizardDialog(Display.getDefault().getActiveShell(), this);
        dialog.open();
    }

    // ========== 向导页面内部类 ==========
    
    private class WelcomePage extends WizardPage {
        public WelcomePage() {
            super("welcomePage");
            setTitle("Welcome to Ruyi Installation");
            setDescription("This wizard will guide you through the Ruyi installation process");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(1, false));
            
            Label label = new Label(container, SWT.WRAP);
            label.setText("The Ruyi provides all the necessary tools and libraries for Ruyi development.\n\n" +
                        "Before proceeding, please ensure you have:\n" +
                        "• At least 500MB of free disk space\n" +
                        "• An active internet connection\n" +
                        "• Administrator privileges (if installing system-wide)");
            label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            
            setControl(container);
        }
    }
    
    private class InstallationCheckPage extends WizardPage {
        private final RuyiInstallManager installManager;
        private final RuyiLogger logger;
        private Label statusLabel;
        private Button forceInstallCheckbox;

        public InstallationCheckPage(RuyiInstallManager installManager, RuyiLogger logger) {
            super("installationCheckPage");
            this.installManager = installManager;
            this.logger = logger;
            setTitle("Installation Check");
            setDescription("Checking your system for Ruyi installation");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(1, false));
            
            statusLabel = new Label(container, SWT.WRAP);
            statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            statusLabel.setText("Checking system requirements...");
            
            forceInstallCheckbox = new Button(container, SWT.CHECK);
            forceInstallCheckbox.setText("Force installation (even if already installed)");
            forceInstallCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
            
            setControl(container);
            checkInstallation();
        }

        private void checkInstallation() {
            getContainer().getShell().getDisplay().asyncExec(() -> {
                try {
                    boolean isInstalled = installManager.isInstalled();
                    if (isInstalled) {
                        statusLabel.setText("Ruyi is already installed on your system.");
                        setPageComplete(true);
                    } else {
                        statusLabel.setText("No existing Ruyi installation found.");
                        setPageComplete(true);
                    }
                } catch (Exception e) {
                    logger.logError("Installation check failed", e);
                    statusLabel.setText("Error checking installation: " + e.getMessage());
                    setPageComplete(false);
                }
            });
        }

        public boolean isForceInstall() {
            return forceInstallCheckbox.getSelection();
        }
    }
    
    private class VersionComparisonPage extends WizardPage {
        private final RuyiInstallManager installManager;
        private final RuyiLogger logger;
        private VersionCompareComposite compareComposite;

        public VersionComparisonPage(RuyiInstallManager installManager, RuyiLogger logger) {
            super("versionComparisonPage");
            this.installManager = installManager;
            this.logger = logger;
            setTitle("Version Comparison");
            setDescription("Compare your current version with the latest available");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(1, false));
            
            compareComposite = new VersionCompareComposite(container);
            compareComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
           
            setControl(container);
            checkVersions();
        }

        private void checkVersions() {
            getContainer().getShell().getDisplay().asyncExec(() -> {
                try {
//                    String currentVersion = installManager.getInstalledVersion();
//                    String latestVersion = installManager.getLatestVersion();
//                    String changelog = installManager.getChangelog(latestVersion);
                	RuyiVersion currentVersion = RuyiManager.getInstalledVersion();
                	String archSuffix = SystemInfo.detectArchitecture().getSuffix();
                	RuyiReleaseInfo latestRelease = RuyiAPI.getLatestRelease(archSuffix);
                    String latestVersion = latestRelease.getVersion();//RuyiManager.getLatestVersion().toString();
                    String changelog = "[Here Need API of 0.32.0 chagelog]";
//                    compareComposite.setVersions(currentVersion, lastestVersion, changelog);
                    
                    compareComposite.setVersions(
                        currentVersion != null ? currentVersion.toString() : "Not installed", 
                        latestVersion,
                        changelog
                    );
                    compareComposite.highlightDifferences();
                    
                    setPageComplete(true);
                } catch (Exception e) {
                    logger.logError("Version check failed", e);
                    setErrorMessage("Failed to check versions: " + e.getMessage());
                    setPageComplete(false);
                }
            });
        }
    }
    
    private class ConfigurationPage extends WizardPage {
        private final RuyiInstallManager installManager;
        private final RuyiLogger logger;
        private Text ruyiInstallPathText;
        
        private Button[] packageIndexRepoCheckboxes;
        private Button customCheckbox;
        private Text repoCustomUrlText;
        private RepoConfig[] repoConfigs;
        
        private Button telemetryCheckbox;
        


        public ConfigurationPage(RuyiInstallManager installManager, RuyiLogger logger) {
            super("configurationPage");
            this.installManager = installManager;
            this.logger = logger;
            setTitle("Configuration");
            setDescription("Configure Ruyi installation options");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(1, false));
            
            // 安装路径
            createRuyiInstallPathConfigUI(container);
            
            // 存储库选择
            createPackageIndexRepoUI(container);
            
            // 遥测选项
            createTelemetryConfigUI(container);
            
            
            setControl(container);
        }

        private void browseForInstallPath() {
            DirectoryDialog dialog = new DirectoryDialog(getShell());
            dialog.setText("Select Installation Directory");
            String path = dialog.open();
            if (path != null) {
                ruyiInstallPathText.setText(path);   //用户自定义安装路径回显
            }
            
            //根据用户修改的路径设置安装路径
            installManager.setInstallPath(path);
        }
        
        private void createRuyiInstallPathConfigUI(Composite parent){
        	Group group = new Group(parent, SWT.NONE);
        	group.setText("Installation Path:");
        	group.setLayout(new GridLayout(2, false));
            group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            
            ruyiInstallPathText = new Text(group, SWT.BORDER);
            ruyiInstallPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            ruyiInstallPathText.setText(installManager.getDefaultInstallPath());   //设置默认安装路径 ~/.local/bin
            
            Button browseButton = new Button(group, SWT.PUSH);
            browseButton.setText("Browse...");
            browseButton.addListener(SWT.Selection, e -> browseForInstallPath());  //提供用户自定义安装路径
        }
        
        private void createPackageIndexRepoUI(Composite parent) {
            Group group = new Group(parent, SWT.NONE);
            group.setText("Package Index Repository");
            group.setLayout(new GridLayout(1, false));
            group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            
            repoConfigs = new RepoConfig[] {
        	    new RepoConfig("China ISCAS Mirror", "https://mirror.iscas.ac.cn/git/ruyisdk/packages-index.git",1),
        	    new RepoConfig("GitHub Repo", "https://github.com/ruyisdk/packages-index.git",2),
        	    new RepoConfig("Custom", "",0) 
        	};
            packageIndexRepoCheckboxes = new Button[repoConfigs.length];
            
            for (int i = 0; i < repoConfigs.length; i++) {
                RepoConfig config = repoConfigs[i];
                
                if("Custom".equals(config.getName())) {
                	Composite row = new Composite(group, SWT.NONE);
                    row.setLayout(new GridLayout(2, false));
                    row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                    
                    packageIndexRepoCheckboxes[i] = new Button(row, SWT.CHECK);
                    packageIndexRepoCheckboxes[i].setText(config.getName() + ": " + config.getUrl());
                    
                    repoCustomUrlText = new Text(row, SWT.BORDER);
                	repoCustomUrlText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                	repoCustomUrlText.setEnabled(false);
                    
                    // 绑定自定义仓库复选框状态变化
                	customCheckbox = packageIndexRepoCheckboxes[i];
                    packageIndexRepoCheckboxes[i].addListener(SWT.Selection, e -> {
                    	repoCustomUrlText.setEnabled(customCheckbox.getSelection());
                    });
                	
                }else {
                	Composite row = new Composite(group, SWT.NONE);
                    row.setLayout(new GridLayout(1, false));
                    row.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
                    
                    packageIndexRepoCheckboxes[i] = new Button(row, SWT.CHECK);
                    packageIndexRepoCheckboxes[i].setText(config.getName() + ": " + config.getUrl());
                    packageIndexRepoCheckboxes[i].setSelection(true);
                }
            }
        }
        public RepoConfig[] getSelectedRepoConfigs() {
            List<RepoConfig> selectedConfigs = new ArrayList<>();
            
            for (int i = 0; i < packageIndexRepoCheckboxes.length; i++) {
                if (packageIndexRepoCheckboxes[i].getSelection()) {
                    // 如果是自定义仓库，更新URL
                    if ("Custom".equals(repoConfigs[i].getName())) {
                        repoConfigs[i].setUrl(repoCustomUrlText.getText().trim());
                    }
                    // 只有当URL不为空时才添加到选中列表
                    if (!repoConfigs[i].getUrl().isEmpty()) {
                        selectedConfigs.add(repoConfigs[i]);
                    }
                }
            }
            // Sort by priority (ascending order)
            selectedConfigs.sort(Comparator.comparingInt(RepoConfig::getPriority));
            
            return selectedConfigs.toArray(new RepoConfig[0]);
        }
        
        private void createTelemetryConfigUI(Composite parent) {
            Group group = new Group(parent, SWT.NONE);
            group.setText("Telemetry:");
            group.setLayout(new GridLayout(1, false));
            group.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

            telemetryCheckbox = new Button(group, SWT.CHECK);
            telemetryCheckbox.setText("Send anonymous usage data to help improve RuyiSDK");
            telemetryCheckbox.setSelection(true);
            telemetryCheckbox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));


            // 创建一个Link控件来显示描述文本和链接
            Link link = new Link(group, SWT.WRAP);
            String description = "RuyiSDK 遥测数据秉持最小化收集信息的原则，尽可能避免收集用户个人身份信息，且采用匿名化方式收集信息用于产品的运营和服务的提升。可前往 Windows > Preferences > Ruyi 中进行设置修改。详见";
            String linkText = "<a href=\"https://ruyisdk.org/docs/legal/privacyPolicy\">RuyiSDK隐私政策</a>";
            link.setText(description + " " + linkText);
//            link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
         // 设置Link控件的布局数据，控制宽度
            GridData linkLayoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
            linkLayoutData.widthHint = 300; // 设置固定宽度，根据需要调整
            link.setLayoutData(linkLayoutData);

            // 添加链接点击事件
            link.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent event) {
                    // 在默认浏览器中打开链接
                    Program.launch(event.text);
                }
            });

            // 设置链接颜色为蓝色
//            link.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_BLUE));
        }
        
        @Override
        public boolean isPageComplete() {
            return ruyiInstallPathText.getText() != null && !ruyiInstallPathText.getText().isEmpty();
        }

        public String getInstallPath() {
            return ruyiInstallPathText.getText();
        }

        public boolean isTelemetryEnabled() {
            return telemetryCheckbox.getSelection();
        }
    }
    
    private class InstallationPage extends WizardPage {
        private final RuyiInstallManager installManager;
        private final RuyiLogger logger;
        private InstallProgressComposite progressComposite;

        public InstallationPage(RuyiInstallManager installManager, RuyiLogger logger) {
            super("installationPage");
            this.installManager = installManager;
            this.logger = logger;
            setTitle("Installation");
            setDescription("Installing Ruyi on your system");
        }

        @Override
        public void createControl(Composite parent) {
            progressComposite = new InstallProgressComposite(parent);
            setControl(progressComposite);
        }

        @Override
        public void setVisible(boolean visible) {
            super.setVisible(visible);
            if (visible) {
                startInstallation();
            }
        }

        private void startInstallation() {
            getContainer().getShell().getDisplay().asyncExec(() -> {
                try {
                    ConfigurationPage configPage = (ConfigurationPage) getWizard().getPage("configurationPage");
                    
                    installManager.setInstallPath(configPage.getInstallPath());
                    installManager.setRepoUrls(configPage.getSelectedRepoConfigs());
                    installManager.setTelemetryEnabled(configPage.isTelemetryEnabled());
                    
                    performInstallation();
                } catch (Exception e) {
                    logger.logError("Installation failed", e);
                    progressComposite.appendLog("Error: " + e.getMessage());
                    setPageComplete(false);
                }
            });
        }

        private void performInstallation() {
            progressComposite.appendLog("Starting Ruyi installation...");
            
            Job installJob = Job.create("Install Ruyi", monitor -> {
                try {
                    installManager.install(monitor, new InstallationListener() {
                        @Override
                        public void progressChanged(int percent, String message) {
                            progressComposite.updateProgress(percent, message);
                        }

                        @Override
                        public void logMessage(String message) {
                            progressComposite.appendLog(message);
                        }
                    });
                    
                    getContainer().getShell().getDisplay().asyncExec(() -> {
                        progressComposite.appendLog("Installation completed successfully!");
                        setPageComplete(true);
                    });
                    
                    return Status.OK_STATUS;
                } catch (Exception e) {
                    logger.logError("Installation failed", e);
                    getContainer().getShell().getDisplay().asyncExec(() -> {
                        progressComposite.appendLog("Installation failed: " + e.getMessage());
                        setPageComplete(false);
                    });
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Installation failed", e);
                }
            });
            
            installJob.schedule();
        }

        public boolean performFinish() {
            return isPageComplete();
        }
    }
    
    private class CompletionPage extends WizardPage {
        private final RuyiInstallManager installManager;
        private final RuyiLogger logger;
        private Label completionLabel;

        public CompletionPage(RuyiInstallManager installManager, RuyiLogger logger) {
            super("completionPage");
            this.installManager = installManager;
            this.logger = logger;
            setTitle("Installation Complete");
            setDescription("Ruyi has been successfully installed");
        }

        @Override
        public void createControl(Composite parent) {
            Composite container = new Composite(parent, SWT.NONE);
            container.setLayout(new GridLayout(1, false));
            
            completionLabel = new Label(container, SWT.WRAP);
            completionLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
            completionLabel.setText("Ruyi has been successfully installed on your system.\n\n" +
                                 "You can now start using Ruyi for your projects.");
            
            setControl(container);
        }

        @Override
        public boolean isPageComplete() {
            return true;
        }
    }
    
    // ========== 辅助接口 ==========
    
    public interface InstallationListener {
        void progressChanged(int percent, String message);
        void logMessage(String message);
    }
}