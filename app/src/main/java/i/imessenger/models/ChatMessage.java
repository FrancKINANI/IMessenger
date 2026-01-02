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
    public List<String> mediaTypes; // "image" or "video"
    public String messageType; // "text", "image", "video", "mixed"

    public ChatMessage() {
        this.messageType = "text";
    }

    public boolean hasMedia() {
        return mediaUrls != null && !mediaUrls.isEmpty();
    }
}
