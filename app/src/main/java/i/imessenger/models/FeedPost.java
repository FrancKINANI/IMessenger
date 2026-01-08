package i.imessenger.models;

import com.google.firebase.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FeedPost {
    private String postId;
    private String authorId;
    private String authorName;
    private String authorImage;
    private String authorRole;
    private String content;
    private List<String> mediaUrls;
    private List<String> mediaTypes; // "image", "video", or "document"
    private List<String> mediaNames; // Original file names for documents
    private Timestamp createdAt;
    private List<String> likedBy;
    private int commentCount;
    private String visibility; // "public", "class", "private"
    private String targetClass; // For class-specific posts
    private int viewCount;

    public FeedPost() {
        // Required for Firestore
        this.mediaUrls = new ArrayList<>();
        this.mediaTypes = new ArrayList<>();
        this.mediaNames = new ArrayList<>();
        this.likedBy = new ArrayList<>();
        this.viewCount = 0;
    }

    public FeedPost(String postId, String authorId, String authorName, String authorImage,
            String authorRole, String content, List<String> mediaUrls,
            List<String> mediaTypes, Timestamp createdAt, String visibility, String targetClass) {
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorImage = authorImage;
        this.authorRole = authorRole;
        this.content = content;
        this.mediaUrls = mediaUrls != null ? mediaUrls : new ArrayList<>();
        this.mediaTypes = mediaTypes != null ? mediaTypes : new ArrayList<>();
        this.createdAt = createdAt;
        this.likedBy = new ArrayList<>();
        this.commentCount = 0;
        this.visibility = visibility;
        this.targetClass = targetClass;
        this.viewCount = 0;
    }

    // Getters and Setters
    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getAuthorId() {
        return authorId;
    }

    public void setAuthorId(String authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getAuthorImage() {
        return authorImage;
    }

    public void setAuthorImage(String authorImage) {
        this.authorImage = authorImage;
    }

    public String getAuthorRole() {
        return authorRole;
    }

    public void setAuthorRole(String authorRole) {
        this.authorRole = authorRole;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<String> getMediaUrls() {
        return mediaUrls;
    }

    public void setMediaUrls(List<String> mediaUrls) {
        this.mediaUrls = mediaUrls;
    }

    public List<String> getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(List<String> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public List<String> getMediaNames() {
        return mediaNames;
    }

    public void setMediaNames(List<String> mediaNames) {
        this.mediaNames = mediaNames;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public List<String> getLikedBy() {
        return likedBy;
    }

    public void setLikedBy(List<String> likedBy) {
        this.likedBy = likedBy;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(String targetClass) {
        this.targetClass = targetClass;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getLikeCount() {
        return likedBy != null ? likedBy.size() : 0;
    }

    public boolean isLikedBy(String userId) {
        return likedBy != null && likedBy.contains(userId);
    }

    public boolean hasMedia() {
        return mediaUrls != null && !mediaUrls.isEmpty();
    }
}
