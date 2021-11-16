package ir.fanap.podstream.entity;


import ir.fanap.podstream.network.response.AvoidObfuscate;

public class FileSetup implements AvoidObfuscate {

    private final String videoAddress;
    private final int quality;
    private final boolean mobile;
    private final long size;
    private final boolean progressive;
    private String streamTopic;
    private String controlTopic;

    public void setStreamTopic(String streamTopic) {
        this.streamTopic = streamTopic;

    }

    public String getVideoAddress() {
        return videoAddress;
    }

    public void setControlTopic(String controlTopic) {
        this.controlTopic = controlTopic;
    }

    public String getUrl(String serverurl, String clientId) {
        String url = serverurl + "register/" +
                "?token=" + clientId +
                "&hashFile=" + videoAddress +
                "&quality=" + quality +
                "&mobile=" + mobile +
                "&progressive=" + progressive +
                "&consumTopic=" + streamTopic +
                "&produceTopic=" + controlTopic;
        return url;
    }

    public FileSetup(Builder builder) {
        this.videoAddress = builder.videoAddress;
        this.quality = builder.quality;
        this.mobile = builder.mobile;
        this.progressive = builder.progressive;
        size = 0;
    }

    public static class Builder implements AvoidObfuscate {

        private String videoAddress;
        private int quality = 240;
        private boolean mobile = true;
        private boolean progressive = true;
        private long size = 0;
        public FileSetup build(String videoAddress) {
            this.videoAddress = videoAddress;
            return new FileSetup(this);
        }

        public Builder setSize(long size) {
            this.size = size;
            return this;
        }
    }


}
