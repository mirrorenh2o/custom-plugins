package org.ruyisdk.core.ruyi.commands;

import org.ruyisdk.core.ruyi.RuyiException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * 命令执行抽象基类
 */
public abstract class RuyiCommand {
    protected String executeCommand(String command) throws RuyiException {
        try {
            Process process = Runtime.getRuntime().exec(command);
            process.waitFor();

            if (process.exitValue() != 0) {
                throw new RuyiException(RuyiException.ErrorCode.COMMAND_EXECUTION_FAILED,
                    "Command failed: " + command + ", exit code: " + process.exitValue());
            }

            BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
            return output.toString().trim();
        } catch (IOException | InterruptedException e) {
            throw new RuyiException(RuyiException.ErrorCode.COMMAND_EXECUTION_FAILED,
                "Failed to execute command: " + command, e);
        }
    }
}