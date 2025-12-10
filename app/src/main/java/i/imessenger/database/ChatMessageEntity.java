package i.imessenger.database;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Date;

import i.imessenger.models.ChatMessage;

@Entity(tableName = "chat_messages")
public class ChatMessageEntity {
    @PrimaryKey(autoGenerate = true)
    public int id;
    
    public String senderId;
    public String receiverId;
    public String message;
    public String dateTime;
    public Long dateObject; // Store as timestamp for sorting
    public String conversionId;
    public String conversionName;
    public String conversionImage;
    public String groupId;

    public ChatMessageEntity() {}

    // Constructor to convert from Firestore model
    public ChatMessageEntity(ChatMessage chatMessage) {
        this.senderId = chatMessage.senderId;
        this.receiverId = chatMessage.receiverId;
        this.message = chatMessage.message;
        this.dateTime = chatMessage.dateTime;
        this.dateObject = chatMessage.dateObject != null ? chatMessage.dateObject.getTime() : null;
        this.conversionId = chatMessage.conversionId;
        this.conversionName = chatMessage.conversionName;
        this.conversionImage = chatMessage.conversionImage;
        this.groupId = chatMessage.groupId;
    }

    public ChatMessage toChatMessage() {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.senderId = this.senderId;
        chatMessage.receiverId = this.receiverId;
        chatMessage.message = this.message;
        chatMessage.dateTime = this.dateTime;
        chatMessage.dateObject = this.dateObject != null ? new Date(this.dateObject) : null;
        chatMessage.conversionId = this.conversionId;
        chatMessage.conversionName = this.conversionName;
        chatMessage.conversionImage = this.conversionImage;
        chatMessage.groupId = this.groupId;
        return chatMessage;
    }
}
