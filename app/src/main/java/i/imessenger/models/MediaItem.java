package i.imessenger.models;

import com.google.firebase.Timestamp;

public class MediaItem {
    private String mediaId;
    private String ownerId;
    private String url;
    private String thumbnailUrl;
    private String type; // "image" or "video"
    private String caption;
    private Timestamp uploadedAt;
    private long sizeBytes;
    private int width;
    private int height;
    private long duration; // For videos in milliseconds

    public MediaItem() {
        // Required for Firestore
    }

    public MediaItem(String mediaId, String ownerId, String url, String thumbnailUrl,
                     String type, String caption, Timestamp uploadedAt, long sizeBytes,
                     int width, int height, long duration) {
        this.mediaId = mediaId;
        this.ownerId = ownerId;
        this.url = url;
        this.thumbnailUrl = thumbnailUrl;
        this.type = type;
        this.caption = caption;
        this.uploadedAt = uploadedAt;
        this.sizeBytes = sizeBytes;
        this.width = width;
        this.height = height;
        this.duration = duration;
    }

    // Getters and Setters
    public String getMediaId() { return mediaId; }
    public void setMediaId(String mediaId) { this.mediaId = mediaId; }

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }

    public String getThumbnailUrl() { return thumbnailUrl; }
    public void setThumbnailUrl(String thumbnailUrl) { this.thumbnailUrl = thumbnailUrl; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public Timestamp getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(Timestamp uploadedAt) { this.uploadedAt = uploadedAt; }

    public long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(long sizeBytes) { this.sizeBytes = sizeBytes; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }

    public boolean isVideo() {
        return "video".equals(type);
    }

    public boolean isImage() {
        return "image".equals(type);
    }

    public String getFormattedSize() {
        if (sizeBytes < 1024) return sizeBytes + " B";
        if (sizeBytes < 1024 * 1024) return String.format("%.1f KB", sizeBytes / 1024.0);
        return String.format("%.1f MB", sizeBytes / (1024.0 * 1024.0));
    }

    public String getFormattedDuration() {
        if (duration <= 0) return "";
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}

