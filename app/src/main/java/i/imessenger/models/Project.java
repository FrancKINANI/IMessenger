package i.imessenger.models;

import java.util.List;

public class Project {
    private String projectId;
    private String name;
    private String description;
    private String projectType; // "PROJECT", "CLUB", "STUDY_GROUP"
    private String creatorId;
    private String creatorName;
    private List<String> members;
    private List<String> admins;
    private String projectImage;
    private long createdAt;
    private long deadline;
    private String status; // "ACTIVE", "COMPLETED", "ARCHIVED"
    private List<String> tasks;

    public Project() {
        // Required for Firestore
    }

    public Project(String projectId, String name, String description, String projectType,
                   String creatorId, String creatorName, List<String> members, List<String> admins,
                   String projectImage, long deadline) {
        this.projectId = projectId;
        this.name = name;
        this.description = description;
        this.projectType = projectType;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.members = members;
        this.admins = admins;
        this.projectImage = projectImage;
        this.createdAt = System.currentTimeMillis();
        this.deadline = deadline;
        this.status = "ACTIVE";
    }

    // Getters and Setters
    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getProjectType() { return projectType; }
    public void setProjectType(String projectType) { this.projectType = projectType; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public List<String> getMembers() { return members; }
    public void setMembers(List<String> members) { this.members = members; }

    public List<String> getAdmins() { return admins; }
    public void setAdmins(List<String> admins) { this.admins = admins; }

    public String getProjectImage() { return projectImage; }
    public void setProjectImage(String projectImage) { this.projectImage = projectImage; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getDeadline() { return deadline; }
    public void setDeadline(long deadline) { this.deadline = deadline; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<String> getTasks() { return tasks; }
    public void setTasks(List<String> tasks) { this.tasks = tasks; }
}

