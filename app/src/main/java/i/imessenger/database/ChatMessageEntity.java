package i.imessenger.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import i.imessenger.models.ChatMessage;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey
    @androidx.annotation.NonNull
    public String id;
    
    public String senderId;
    public String receiverId;
    public String message;
    public String dateTime;
    public Long dateObject; // Store as timestamp for sorting
    public String conversionId;
    public String conversionName;
    public String conversionImage;
    public String groupId;

    // Media fields - stored as comma-separated strings for simplicity
    public String mediaUrls; // Comma separated
    public String mediaTypes; // Comma separated
    public String messageType;

    public ChatMessageEntity() {}

    // Constructor to convert from Firestore model
    public ChatMessageEntity(ChatMessage chatMessage) {
        this.id = chatMessage.id;
        this.senderId = chatMessage.senderId;
        this.receiverId = chatMessage.receiverId;
        this.message = chatMessage.message;
        this.dateTime = chatMessage.dateTime;
        this.dateObject = chatMessage.dateObject != null ? chatMessage.dateObject.getTime() : null;
        this.conversionId = chatMessage.conversionId;
        this.conversionName = chatMessage.conversionName;
        this.conversionImage = chatMessage.conversionImage;
        this.groupId = chatMessage.groupId;
        this.messageType = chatMessage.messageType;

        // Convert lists to comma-separated strings
        if (chatMessage.mediaUrls != null && !chatMessage.mediaUrls.isEmpty()) {
            this.mediaUrls = String.join(",", chatMessage.mediaUrls);
        }
        if (chatMessage.mediaTypes != null && !chatMessage.mediaTypes.isEmpty()) {
            this.mediaTypes = String.join(",", chatMessage.mediaTypes);
        }
    }

    public ChatMessage toChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.id = this.id;
        chatMessage.senderId = this.senderId;
        chatMessage.receiverId = this.receiverId;
        chatMessage.message = this.message;
        chatMessage.dateTime = this.dateTime;
        chatMessage.dateObject = this.dateObject != null ? new Date(this.dateObject) : null;
        chatMessage.conversionId = this.conversionId;
        chatMessage.conversionName = this.conversionName;
        chatMessage.conversionImage = this.conversionImage;
        chatMessage.groupId = this.groupId;
        chatMessage.messageType = this.messageType;

        // Convert comma-separated strings back to lists
        if (this.mediaUrls != null && !this.mediaUrls.isEmpty()) {
            chatMessage.mediaUrls = new ArrayList<>();
            for (String url : this.mediaUrls.split(",")) {
                chatMessage.mediaUrls.add(url);
            }
        }
        if (this.mediaTypes != null && !this.mediaTypes.isEmpty()) {
            chatMessage.mediaTypes = new ArrayList<>();
            for (String type : this.mediaTypes.split(",")) {
                chatMessage.mediaTypes.add(type);
            }
        }

        return chatMessage;
    }
}
