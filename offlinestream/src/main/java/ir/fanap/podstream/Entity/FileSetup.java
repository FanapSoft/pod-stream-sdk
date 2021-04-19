package ir.fanap.podstream.Entity;


public class FileSetup {

    private final String baseUrl;
    private final String clientId;
    private final String videoAddress;
    private final int quality;
    private final boolean mobile;
    private final boolean progressive;
    private  String streamTopic;
    private  String controlTopic;

    public void setStreamTopic(String streamTopic) {
        this.streamTopic = streamTopic;

    }

    public String getClientId() {
        return clientId;
    }

    public String getVideoAddress() {
        return videoAddress;
    }

    public void setControlTopic(String controlTopic) {
        this.controlTopic = controlTopic;
    }

    public String getUrl() {
        String url = baseUrl +
                "?token=" + clientId +
                "&hashFile=" + videoAddress +
                "&quality=" + quality +
                "&mobile=" + mobile +
                "&progressive=" + progressive+
                "&consumTopic=" + streamTopic+
                "&produceTopic=" + controlTopic;

        return url;
    }

    public FileSetup(Builder builder) {

        this.baseUrl = builder.baseUrl;
        this.clientId = builder.clientId;
        this.videoAddress = builder.videoAddress;
        this.quality = builder.quality;
        this.mobile = builder.mobile;
        this.progressive = builder.progressive;
    }

    public static class Builder {

        String baseUrl = "http://192.168.112.32:80/register/";
        String clientId;
        String videoAddress;
        int quality = 240;
        boolean mobile = true;
        boolean progressive = true;

        public Builder deactiveProgressive() {
            progressive = false;
            return this;
        }

        public Builder deactiveMobile() {
            mobile = false;
            return this;
        }

        public Builder changeBaseUrl(String url) {
            baseUrl = url;
            return this;
        }

        public Builder changeQuality(int quality) {
            this.quality = quality;
            return this;
        }


        public FileSetup build(String clientId, String videoAddress) {
            this.clientId = clientId;
            this.videoAddress = videoAddress;
            return new FileSetup(this);
        }

    }


}
