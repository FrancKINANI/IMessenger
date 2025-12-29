package i.imessenger.models;

import java.util.List;

public class CalendarEvent {
    private String eventId;
    private String title;
    private String description;
    private long startTime;
    private long endTime;
    private String creatorId;
    private String creatorName;
    private String eventType; // "PERSONAL", "CLASS", "SCHOOL"
    private String targetClass; // For class-specific events (e.g., "Level 1", "Level 2")
    private List<String> targetUsers; // Specific users who can see this event
    private String color;
    private boolean isRecurring;
    private String recurrenceRule; // "DAILY", "WEEKLY", "MONTHLY"
    private long createdAt;

    public CalendarEvent() {
        // Required for Firestore
    }

    public CalendarEvent(String eventId, String title, String description, long startTime, long endTime,
                         String creatorId, String creatorName, String eventType, String targetClass,
                         List<String> targetUsers, String color, boolean isRecurring, String recurrenceRule) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.startTime = startTime;
        this.endTime = endTime;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.eventType = eventType;
        this.targetClass = targetClass;
        this.targetUsers = targetUsers;
        this.color = color;
        this.isRecurring = isRecurring;
        this.recurrenceRule = recurrenceRule;
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }

    public String getCreatorId() { return creatorId; }
    public void setCreatorId(String creatorId) { this.creatorId = creatorId; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getTargetClass() { return targetClass; }
    public void setTargetClass(String targetClass) { this.targetClass = targetClass; }

    public List<String> getTargetUsers() { return targetUsers; }
    public void setTargetUsers(List<String> targetUsers) { this.targetUsers = targetUsers; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isRecurring() { return isRecurring; }
    public void setRecurring(boolean recurring) { isRecurring = recurring; }

    public String getRecurrenceRule() { return recurrenceRule; }
    public void setRecurrenceRule(String recurrenceRule) { this.recurrenceRule = recurrenceRule; }

    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
}

