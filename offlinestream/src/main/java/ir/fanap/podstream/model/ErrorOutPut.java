package ir.fanap.podstream.model;

public class ErrorOutPut {
    private String errorMessage;
    private long errorCode;

    public ErrorOutPut(){}

    public ErrorOutPut(boolean hasError, String errorMessage, long errorCode) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public long getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(long errorCode) {
        this.errorCode = errorCode;
    }

}
