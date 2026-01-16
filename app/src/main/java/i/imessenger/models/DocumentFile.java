package i.imessenger.models;

import com.google.firebase.Timestamp;

public class DocumentFile {
    private String id;
    private String name;
    private String url;
    private String type; // pdf, ppt, etc.
    private long sizeBytes;
    private String uploaderId;
    private String uploaderName;
    private Timestamp createdAt;
    private String folder; // "General", "Courses", "Exams"
    private String targetClass; // For filtering by level

    public DocumentFile() {
        // Required for Firestore
    }

    public DocumentFile(String id, String name, String url, String type, long sizeBytes, String uploaderId,
            String uploaderName, Timestamp createdAt, String folder, String targetClass) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.type = type;
        this.sizeBytes = sizeBytes;
        this.uploaderId = uploaderId;
        this.uploaderName = uploaderName;
        this.createdAt = createdAt;
        this.folder = folder;
        this.targetClass = targetClass;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public long getSizeBytes() {
        return sizeBytes;
    }

    public void setSizeBytes(long sizeBytes) {
        this.sizeBytes = sizeBytes;
    }

    public String getUploaderId() {
        return uploaderId;
    }

    public void setUploaderId(String uploaderId) {
        this.uploaderId = uploaderId;
    }

    public String getUploaderName() {
        return uploaderName;
    }

    public void setUploaderName(String uploaderName) {
        this.uploaderName = uploaderName;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getFolder() {
        return folder;
    }

    public void setFolder(String folder) {
        this.folder = folder;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }
}
