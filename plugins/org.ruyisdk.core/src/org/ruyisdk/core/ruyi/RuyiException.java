package org.ruyisdk.core.ruyi;

/**
 * Ruyi SDK 自定义异常
 */
public class RuyiException extends Exception {
    private final ErrorCode errorCode;

    public RuyiException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public RuyiException(ErrorCode errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public enum ErrorCode {
        COMMAND_EXECUTION_FAILED,
        VERSION_CHECK_FAILED,
        NETWORK_ERROR,
        PERMISSION_DENIED,
        INVALID_VERSION_FORMAT,
        UNSUPPORTED_ARCHITECTURE
    }
}