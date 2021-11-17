package ir.fanap.podstreamsdkexample.data;

public class VideoItem {
   private String videoName;
   private String videoHash;
   private String videoQuality;
   private String size;

    public VideoItem(String videoName, String videoHash, String videoQuality, String size) {
        this.videoName = videoName;
        this.videoHash = videoHash;
        this.videoQuality = videoQuality;
        this.size = size;
    }

    public String getVideoName() {
        return videoName;
    }

    public void setVideoName(String videoName) {
        this.videoName = videoName;
    }

    public String getVideoHash() {
        return videoHash;
    }

    public void setVideoHash(String videoHash) {
        this.videoHash = videoHash;
    }

    public String getVideoQuality() {
        return videoQuality;
    }

    public void setVideoQuality(String videoQuality) {
        this.videoQuality = videoQuality;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getSize() {
        return size;
    }
}
