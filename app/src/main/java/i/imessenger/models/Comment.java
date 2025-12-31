package i.imessenger.models;

import com.google.firebase.Timestamp;

public class Comment {
    private String commentId;
    private String postId;
    private String authorId;
    private String authorName;
    private String authorImage;
    private String content;
    private Timestamp createdAt;

    public Comment() {
        // Required for Firestore
    }

    public Comment(String commentId, String postId, String authorId, String authorName,
                   String authorImage, String content, Timestamp createdAt) {
        this.commentId = commentId;
        this.postId = postId;
        this.authorId = authorId;
        this.authorName = authorName;
        this.authorImage = authorImage;
        this.content = content;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public String getCommentId() { return commentId; }
    public void setCommentId(String commentId) { this.commentId = commentId; }

    public String getPostId() { return postId; }
    public void setPostId(String postId) { this.postId = postId; }

    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

    public String getAuthorImage() { return authorImage; }
    public void setAuthorImage(String authorImage) { this.authorImage = authorImage; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}

