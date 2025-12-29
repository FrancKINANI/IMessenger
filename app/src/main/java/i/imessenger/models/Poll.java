package i.imessenger.models;

import java.util.List;
import java.util.Map;

public class Poll {
    private String pollId;
    private String question;
    private List<String> options;
    private Map<String, List<String>> votes; // option -> list of user IDs who voted
    private String creatorId;
    private String creatorName;
    private String projectId; // Associated project/group
    private long createdAt;
    private long expiresAt;
    private boolean isAnonymous;
    private boolean allowMultipleVotes;
    private boolean isActive;

    public Poll() {
        // Required for Firestore
    }

    public Poll(String pollId, String question, List<String> options, String creatorId,
                String creatorName, String projectId, long expiresAt, boolean isAnonymous,
                boolean allowMultipleVotes) {
        this.pollId = pollId;
        this.question = question;
        this.options = options;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.projectId = projectId;
        this.createdAt = System.currentTimeMillis();
        this.expiresAt = expiresAt;
        this.isAnonymous = isAnonymous;
        this.allowMultipleVotes = allowMultipleVotes;
        this.isActive = true;
    }

    // Getters and Setters
    public String getPollId() { return pollId; }
    public void setPollId(String pollId) { this.pollId = pollId; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }

    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }

    public Map<String, List<String>> getVotes() { return votes; }
    public void setVotes(Map<String, List<String>> votes) { this.votes = votes; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }

    public long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(long expiresAt) { this.expiresAt = expiresAt; }

    public boolean isAnonymous() { return isAnonymous; }
    public void setAnonymous(boolean anonymous) { isAnonymous = anonymous; }

    public boolean isAllowMultipleVotes() { return allowMultipleVotes; }
    public void setAllowMultipleVotes(boolean allowMultipleVotes) { this.allowMultipleVotes = allowMultipleVotes; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public int getTotalVotes() {
        if (votes == null) return 0;
        int total = 0;
        for (List<String> voterList : votes.values()) {
            if (voterList != null) {
                total += voterList.size();
            }
        }
        return total;
    }
}

