package i.imessenger.viewmodels;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import i.imessenger.models.ChatMessage;
import i.imessenger.models.Group;
import i.imessenger.repositories.ChatRepository;

public class ChatViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private LiveData<List<ChatMessage>> messagesLiveData;
    private MutableLiveData<Boolean> isUploading = new MutableLiveData<>(false);
    private MutableLiveData<String> uploadError = new MutableLiveData<>();

    private String currentUserId;
    private String receiverId;
    private String groupId;

    public ChatViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application);
        chatRepository.getCurrentUser().observeForever(user -> {
            if (user != null) currentUserId = user.getUid();
        });
    }

    public LiveData<List<ChatMessage>> getMessages() {
        return messagesLiveData;
    }

    public LiveData<Boolean> getIsUploading() {
        return isUploading;
    }

    public LiveData<String> getUploadError() {
        return uploadError;
    }

    public LiveData<Group> getGroupInfo(String groupId) {
        return chatRepository.getGroupInfo(groupId);
    }

    public void initChat(String receiverId, String groupId) {
        this.receiverId = receiverId;
        this.groupId = groupId;
        
        // Start listening to Firestore changes (sync to DB)
        chatRepository.syncMessages(receiverId, groupId);
        
        // Expose DB data to UI
        messagesLiveData = chatRepository.getMessages(receiverId, groupId);
    }

    public void sendMessage(String messageText, String conversionId, String conversionName, String conversionImage) {
        if (currentUserId == null) return;
        
        ChatMessage message = new ChatMessage();
        message.senderId = currentUserId;
        message.receiverId = receiverId;
        message.groupId = groupId;
        message.message = messageText;
        message.messageType = "text";
        message.dateTime = getReadableDateTime(new java.util.Date());
        message.dateObject = new java.util.Date();
        
        // Conversion info
        message.conversionId = conversionId;
        message.conversionName = conversionName;
        message.conversionImage = conversionImage;

        chatRepository.sendMessage(message);
    }
    
    public void sendMediaMessage(String messageText, List<Uri> mediaUris, List<String> mediaTypes,
                                  String conversionId, String conversionName, String conversionImage) {
        if (currentUserId == null) return;
        if (mediaUris == null || mediaUris.isEmpty()) {
            if (messageText != null && !messageText.trim().isEmpty()) {
                sendMessage(messageText, conversionId, conversionName, conversionImage);
            }
            return;
        }

        isUploading.setValue(true);

        ChatMessage message = new ChatMessage();
        message.senderId = currentUserId;
        message.receiverId = receiverId;
        message.groupId = groupId;
        message.message = messageText != null ? messageText : "";
        message.dateTime = getReadableDateTime(new java.util.Date());
        message.dateObject = new java.util.Date();
        message.mediaTypes = mediaTypes;

        // Determine message type
        if (mediaTypes.contains("video")) {
            message.messageType = mediaTypes.contains("image") ? "mixed" : "video";
        } else {
            message.messageType = "image";
        }

        // Conversion info
        message.conversionId = conversionId;
        message.conversionName = conversionName;
        message.conversionImage = conversionImage;

        chatRepository.sendMediaMessage(message, mediaUris, new ChatRepository.MediaUploadCallback() {
            @Override
            public void onSuccess() {
                isUploading.postValue(false);
            }

            @Override
            public void onError(String error) {
                isUploading.postValue(false);
                uploadError.postValue(error);
            }
        });
    }

    private String getReadableDateTime(java.util.Date date) {
        return new java.text.SimpleDateFormat("MMMM dd, yyyy - hh:mm a", java.util.Locale.getDefault()).format(date);
    }
    @Override
    protected void onCleared() {
        super.onCleared();
        chatRepository.cleanup();
    }
}
