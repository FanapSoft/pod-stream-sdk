package ir.fanap.podstream.util;

public class PodOfflineStreamException extends Exception {

    private String uniqueId;
    private String token;
    private int code;

    public PodOfflineStreamException(String uniqueId, String token) {
        this.uniqueId = uniqueId;
        this.token = token;
    }

    public PodOfflineStreamException(String message, String uniqueId, String token) {
        super(message);
        this.uniqueId = uniqueId;
        this.token = token;
    }

    public PodOfflineStreamException(String message, String uniqueId, String token, int code) {
        super(message);
        this.uniqueId = uniqueId;
        this.token = token;
        this.code = code;
    }

    public PodOfflineStreamException(String message, int code) {
        super(message);
        this.code = code;
    }

    public PodOfflineStreamException(String message, int code, String uniqueId) {
        super(message);
        this.code = code;
        this.uniqueId = uniqueId;
    }

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
