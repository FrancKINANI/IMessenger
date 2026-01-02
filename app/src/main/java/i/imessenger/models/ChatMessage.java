package i.imessenger.models;

import java.util.Date;
import java.util.List;

public class ChatMessage {
    public String id;
    public String senderId, receiverId, message, dateTime;
    public Date dateObject;
    public String conversionId, conversionName, conversionImage;
    public String groupId;

    // Media fields
    public List<String> mediaUrls;
    public List<String> mediaTypes; // "image", "video", or "document"
    public List<String> mediaNames; // Original file names for documents
    public String messageType; // "text", "image", "video", "document", "mixed"

    public ChatMessage() {
        this.messageType = "text";
    }

    public boolean hasMedia() {
        return mediaUrls != null && !mediaUrls.isEmpty();
    }

    public boolean hasDocuments() {
        if (mediaTypes == null) return false;
        for (String type : mediaTypes) {
            if ("document".equals(type)) return true;
        }
        return false;
    }
}
