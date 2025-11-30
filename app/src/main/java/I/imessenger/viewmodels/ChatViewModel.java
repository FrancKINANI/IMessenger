package I.imessenger.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import I.imessenger.models.ChatMessage;
import I.imessenger.repositories.ChatRepository;

public class ChatViewModel extends ViewModel {

    private final ChatRepository chatRepository;
    private final MutableLiveData<List<ChatMessage>> messagesLiveData = new MutableLiveData<>();
    private final List<ChatMessage> messageList = new ArrayList<>();
    
    private String currentUserId;
    private String receiverId;
    private String groupId;

    public ChatViewModel() {
        chatRepository = new ChatRepository();
        chatRepository.getCurrentUser().observeForever(user -> {
            if (user != null) currentUserId = user.getUid();
        });
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messagesLiveData;
    }

    public void initChat(String receiverId, String groupId) {
        this.receiverId = receiverId;
        this.groupId = groupId;
        messageList.clear();
        listenMessages();
    }

    private void listenMessages() {
        if (groupId != null) {
            chatRepository.getChatCollection()
                    .whereEqualTo("groupId", groupId)
                    .addSnapshotListener(eventListener);
        } else if (receiverId != null && currentUserId != null) {
            chatRepository.getChatCollection()
                    .whereEqualTo("senderId", currentUserId)
                    .whereEqualTo("receiverId", receiverId)
                    .addSnapshotListener(eventListener);
            chatRepository.getChatCollection()
                    .whereEqualTo("senderId", receiverId)
                    .whereEqualTo("receiverId", currentUserId)
                    .addSnapshotListener(eventListener);
        }
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) return;
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = documentChange.getDocument().toObject(ChatMessage.class);
                    // Avoid duplicates if listener triggers multiple times or for both queries
                    if (!containsMessage(chatMessage)) {
                        messageList.add(chatMessage);
                    }
                }
            }
            Collections.sort(messageList, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            messagesLiveData.setValue(messageList);
        }
    };
    
    private boolean containsMessage(ChatMessage newMessage) {
        for (ChatMessage msg : messageList) {
            // Assuming dateObject is unique enough or we should use ID
            if (msg.dateObject != null && msg.dateObject.equals(newMessage.dateObject) && 
                msg.message.equals(newMessage.message) && 
                msg.senderId.equals(newMessage.senderId)) {
                return true;
            }
        }
        return false;
    }

    public void sendMessage(String messageText, String conversionId, String conversionName, String conversionImage) {
        if (currentUserId == null) return;
        
        ChatMessage message = new ChatMessage();
        message.senderId = currentUserId;
        message.receiverId = receiverId;
        message.groupId = groupId;
        message.message = messageText;
        message.dateTime = getReadableDateTime(new java.util.Date());
        message.dateObject = new java.util.Date();
        
        // Conversion info (optional/legacy)
        message.conversionId = conversionId;
        message.conversionName = conversionName;
        message.conversionImage = conversionImage;

        chatRepository.sendMessage(message);
    }
    
    private String getReadableDateTime(java.util.Date date) {
        return new java.text.SimpleDateFormat("MMMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(date);
    }
}
