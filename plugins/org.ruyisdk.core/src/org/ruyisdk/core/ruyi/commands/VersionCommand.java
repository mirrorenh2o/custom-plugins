package org.ruyisdk.core.ruyi.commands;

import org.ruyisdk.core.ruyi.RuyiException;

/**
 * 版本相关命令
 */
public class VersionCommand extends RuyiCommand {
    private static final String VERSION_COMMAND = "ruyi -V";

    public static boolean checkInstalled() {
        try {
            Process p = Runtime.getRuntime().exec(VERSION_COMMAND);
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static String getLocalVersion() throws RuyiException {
        VersionCommand cmd = new VersionCommand();
        String output = cmd.executeCommand(VERSION_COMMAND);
        return parseVersionOutput(output);
    }

    public static String getLatestRemoteVersion() throws RuyiException {
        // 实际实现中应该使用HTTP客户端获取版本
        // 这里简化为模拟实现
        return "1.0.0"; // 从网络获取最新版本
    }

    private static String parseVersionOutput(String output) throws RuyiException {
        // 示例输出: "ruyi version 1.0.0"
        String[] parts = output.split(" ");
        if (parts.length < 3) {
            throw new RuyiException(RuyiException.ErrorCode.VERSION_CHECK_FAILED,
                "Invalid version output format: " + output);
        }
        return parts[2];
    }
}