package i.imessenger.models;

import com.google.firebase.Timestamp;

public class Report {
    private String id;
    private String reporterId;
    private String targetId;
    private String targetType; // POST, USER
    private String reason;
    private Timestamp timestamp;
    private String status; // PENDING, RESOLVED

    public Report() {
    }

    public Report(String id, String reporterId, String targetId, String targetType, String reason,
            Timestamp timestamp) {
        this.id = id;
        this.reporterId = reporterId;
        this.targetId = targetId;
        this.targetType = targetType;
        this.reason = reason;
        this.timestamp = timestamp;
        this.status = "PENDING";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getReporterId() {
        return reporterId;
    }

    public void setReporterId(String reporterId) {
        this.reporterId = reporterId;
    }

    public String getTargetId() {
        return targetId;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public String getTargetType() {
        return targetType;
    }

    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
