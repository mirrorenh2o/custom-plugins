package org.ruyisdk.ruyi.core;

public class VersionCheckResult {
    private final boolean needsUpdate;
    private final String localVersion;
    private final String latestVersion;
    private final String changelog;
    private final String message;

    public VersionCheckResult(boolean needsUpdate, 
                            String localVersion, 
                            String latestVersion,
                            String changelog,
                            String message) {
        this.needsUpdate = needsUpdate;
        this.localVersion = localVersion;
        this.latestVersion = latestVersion;
        this.changelog = changelog;
        this.message = message;
    }

    /**
     * 是否需要更新
     */
    public boolean needsUpdate() {
        return needsUpdate;
    }

    /**
     * 获取本地安装的版本号
     */
    public String getLocalVersion() {
        return localVersion;
    }

    /**
     * 获取服务器最新版本号
     */
    public String getLatestVersion() {
        return latestVersion;
    }

    /**
     * 获取版本更新日志
     */
    public String getChangelog() {
        return changelog;
    }

    /**
     * 获取检查结果消息
     */
    public String getMessage() {
        return message;
    }

    /**
     * 创建"需要更新"的结果对象
     */
    public static VersionCheckResult needsUpdate(String localVersion, 
                                              String latestVersion,
                                              String changelog) {
        return new VersionCheckResult(
            true,
            localVersion,
            latestVersion,
            changelog,
            String.format("Update available: %s → %s", localVersion, latestVersion)
        );
    }

    /**
     * 创建"已是最新版本"的结果对象
     */
    public static VersionCheckResult upToDate(String currentVersion) {
        return new VersionCheckResult(
            false,
            currentVersion,
            currentVersion,
            "",
            "Ruyi is up to date (version " + currentVersion + ")"
        );
    }

    /**
     * 创建"未安装"的结果对象
     */
    public static VersionCheckResult notInstalled() {
        return new VersionCheckResult(
            false,
            null,
            null,
            null,
            "Ruyi is not installed"
        );
    }

    /**
     * 创建"检查失败"的结果对象
     */
    public static VersionCheckResult checkFailed(String errorMessage) {
        return new VersionCheckResult(
            false,
            null,
            null,
            null,
            "Version check failed: " + errorMessage
        );
    }
}