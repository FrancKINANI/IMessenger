package i.imessenger.models;

public class User {
    private String uid;
    private String email;
    private String fullName;
    private String role;
    private String level;
    private String profileImage;
    private String fcmToken;
    private String groups;

    public User() {
        // Required for Firestore
    }

    public User(String uid, String email, String fullName, String role, String level, String profileImage, String fcmToken, String groups) {
        this.uid = uid;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.level = level;
        this.profileImage = profileImage;
        this.fcmToken = fcmToken;
        this.groups = groups;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public String getFcmToken() {
        return fcmToken;
    }

    public void setFcmToken(String fcmToken) {
        this.fcmToken = fcmToken;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }
}
