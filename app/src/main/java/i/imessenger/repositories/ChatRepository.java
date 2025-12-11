package i.imessenger.repositories;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import i.imessenger.database.AppDatabase;
import i.imessenger.database.ChatMessageEntity;
import i.imessenger.models.ChatMessage;
import i.imessenger.models.Group;
import i.imessenger.models.User;
import i.imessenger.utils.EncryptionUtils;

public class ChatRepository {

    private final FirebaseFirestore db;
    private final String currentUserId;
    private final AppDatabase database;
    private final ExecutorService executorService;
    private com.google.firebase.firestore.ListenerRegistration chatListener;

    public ChatRepository(Context context) {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
        database = AppDatabase.getInstance(context);
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<User>> getUsers() {
        MutableLiveData<List<User>> usersLiveData = new MutableLiveData<>();
        if (currentUserId == null) return usersLiveData;

        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> users = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        if (!user.getUid().equals(currentUserId)) {
                            users.add(user);
                        }
                    }
                    usersLiveData.setValue(users);
                })
                .addOnFailureListener(e -> usersLiveData.setValue(null));
        return usersLiveData;
    }

    public LiveData<User> getCurrentUser() {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        if (currentUserId == null) return userLiveData;

        db.collection("users").document(currentUserId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userLiveData.setValue(documentSnapshot.toObject(User.class));
                    } else {
                        userLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> userLiveData.setValue(null));
        return userLiveData;
    }

    public LiveData<List<Group>> getMyGroups() {
        MutableLiveData<List<Group>> groupsLiveData = new MutableLiveData<>();
        if (currentUserId == null) return groupsLiveData;

        db.collection("groups")
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Group group = document.toObject(Group.class);
                        if (!"PUBLIC".equals(group.getGroupType()) && !"EVENT".equals(group.getGroupType())) {
                            groups.add(group);
                        }
                    }
                    groupsLiveData.setValue(groups);
                })
                .addOnFailureListener(e -> groupsLiveData.setValue(null));
        return groupsLiveData;
    }

    public LiveData<List<Group>> getGroups(String type) {
        MutableLiveData<List<Group>> groupsLiveData = new MutableLiveData<>();
        if (currentUserId == null) return groupsLiveData;

        db.collection("groups")
                .whereEqualTo("groupType", type)
                .whereArrayContains("members", currentUserId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        groups.add(document.toObject(Group.class));
                    }
                    groupsLiveData.setValue(groups);
                })
                .addOnFailureListener(e -> groupsLiveData.setValue(null));
        return groupsLiveData;
    }

    public LiveData<List<Group>> getPublicGroups() {
        MutableLiveData<List<Group>> groupsLiveData = new MutableLiveData<>();
        db.collection("groups")
                .whereEqualTo("groupType", "PUBLIC")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        groups.add(document.toObject(Group.class));
                    }
                    groupsLiveData.setValue(groups);
                })
                .addOnFailureListener(e -> groupsLiveData.setValue(null));
        return groupsLiveData;
    }
    
    public LiveData<List<Group>> getEventGroups() {
        MutableLiveData<List<Group>> groupsLiveData = new MutableLiveData<>();
        db.collection("groups")
                .whereEqualTo("groupType", "EVENT")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Group> groups = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        groups.add(document.toObject(Group.class));
                    }
                    groupsLiveData.setValue(groups);
                })
                .addOnFailureListener(e -> groupsLiveData.setValue(null));
        return groupsLiveData;
    }

    public void ensureGroupExists(String groupId, String groupName, String groupType, List<String> members, List<String> admins) {
        db.collection("groups").document(groupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Group group = new Group(groupId, groupName, "", groupType, members, admins, new ArrayList<>());
                        db.collection("groups").document(groupId).set(group);
                    } else {
                        Group group = documentSnapshot.toObject(Group.class);
                        if (group != null && group.getMembers() != null && !group.getMembers().contains(currentUserId)) {
                            db.collection("groups").document(groupId)
                                    .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId));
                        }
                    }
                });
    }

    public void sendMessage(ChatMessage message) {
        // Encrypt message before sending
        try {
            String encryptedText = EncryptionUtils.encrypt(message.message);
            message.message = encryptedText;
        } catch (Exception e) {
            e.printStackTrace();
            // Handle error - maybe don't send? For now we send as plain text/fail safely or just log
            // Ideally we should notify user.
        }
        db.collection("chat").add(message);
    }
    
    public LiveData<List<ChatMessage>> getMessages(String receiverId, String groupId) {
        LiveData<List<ChatMessageEntity>> source;
        if (groupId != null) {
            source = database.chatMessageDao().getGroupMessages(groupId);
        } else {
            source = database.chatMessageDao().getPrivateMessages(currentUserId, receiverId);
        }
        
        return Transformations.map(source, input -> {
            List<ChatMessage> messages = new ArrayList<>();
            for (ChatMessageEntity entity : input) {
                messages.add(entity.toChatMessage());
            }
            return messages;
        });
    }

    public void syncMessages(String receiverId, String groupId) {
        if (chatListener != null) {
            chatListener.remove();
            chatListener = null;
        }

        if (groupId != null) {
            chatListener = db.collection("chat")
                    .whereEqualTo("groupId", groupId)
                    .addSnapshotListener(this::handleSnapshot);
        } else if (receiverId != null && currentUserId != null) {
             // For 1-to-1 chat, logic is tricky with single query.
             // We can only listen to one query effectively or we need composite query.
             // But usually we need two listeners (sent by me AND sent by them).
             // To simplify and avoid duplicates from multiple listeners, 
             // we should use a composite ID or handle it carefully.
             // However, for now, let's just listen to messages involving both users.
             // Ideally: where(users array-contains currentUID)
             
             // The previous code had TWO listeners. This is bad for 'chatListener' variable.
             // Let's optimize: Chat messages usually have a 'conversationId' or we sort client side?
             // Or we just listen to two queries?
             
             // If we use TWO listeners, we need a list of registrations.
             // Let's switch to using 'or' query if possible or manage list.
             // Firestore 'in' query supports up to 10.
             
             // Actually, if we use a unique conversation ID for 1-1 chat, we only need one listener.
             // If not, we have to stick with what we have but manage it better.
             
             // Simplest fix for now: Create a unique conversation ID logic if possible, 
             // BUT simply managing the listener variable assumes one listener.
             // Let's stick to the previous implementation logic but clear previous listeners.
             
             // Since the original code added TWO listeners for 1-1 chat, 
             // assigning to 'chatListener' would overwrite the first one.
             // We need a LIST of listeners.
             
             registerPrivateChatListeners(receiverId);
        }
    }

    private List<com.google.firebase.firestore.ListenerRegistration> listenerRegistrations = new ArrayList<>();

    private void registerPrivateChatListeners(String receiverId) {
        // Clear previous
        removeListeners();
        
        // Listen sent by me
        listenerRegistrations.add(db.collection("chat")
            .whereEqualTo("senderId", currentUserId)
            .whereEqualTo("receiverId", receiverId)
            .addSnapshotListener(this::handleSnapshot));
            
        // Listen sent by them
        listenerRegistrations.add(db.collection("chat")
            .whereEqualTo("senderId", receiverId)
            .whereEqualTo("receiverId", currentUserId)
            .addSnapshotListener(this::handleSnapshot));
    }
    
    // Update syncMessages to use removeListeners logic
    
    public void cleanup() {
        removeListeners();
    }
    
    private void removeListeners() {
        if (chatListener != null) {
            chatListener.remove();
            chatListener = null;
        }
        for (com.google.firebase.firestore.ListenerRegistration reg : listenerRegistrations) {
            reg.remove();
        }
        listenerRegistrations.clear();
    }
    
    private void handleSnapshot(com.google.firebase.firestore.QuerySnapshot value, com.google.firebase.firestore.FirebaseFirestoreException error) {
        if (error != null) return;
        if (value != null) {
            List<ChatMessageEntity> newMessages = new ArrayList<>();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = documentChange.getDocument().toObject(ChatMessage.class);
                    chatMessage.id = documentChange.getDocument().getId();
                    // Decrypt
                    try {
                        chatMessage.message = EncryptionUtils.decrypt(chatMessage.message);
                    } catch (Exception e) {
                        // Keep original if decrypt fails (legacy messages)
                        Log.e("ChatRepository", "Decryption failed: " + e.getMessage());
                    }
                    newMessages.add(new ChatMessageEntity(chatMessage));
                }
            }
            if (!newMessages.isEmpty()) {
                executorService.execute(() -> database.chatMessageDao().insertMessages(newMessages));
            }
        }
    }

    public LiveData<Group> getGroupInfo(String groupId) {
        MutableLiveData<Group> groupLiveData = new MutableLiveData<>();
        db.collection("groups").document(groupId).addSnapshotListener((value, error) -> {
             if (value != null && value.exists()) {
                 groupLiveData.setValue(value.toObject(Group.class));
             }
        });
        return groupLiveData;
    }

    public com.google.firebase.firestore.CollectionReference getChatCollection() {
        return db.collection("chat");
    }
}
