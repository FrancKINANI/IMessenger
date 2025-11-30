package I.imessenger.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import I.imessenger.models.Group;
import I.imessenger.models.User;

public class ChatRepository {

    private final FirebaseFirestore db;
    private final String currentUserId;

    public ChatRepository() {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
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
                        // Filter out PUBLIC and EVENT if needed, or handle in ViewModel
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
                        Group group = new Group(groupId, groupName, "", groupType, members, admins);
                        db.collection("groups").document(groupId).set(group);
                    } else {
                        // Ensure current user is member if applicable
                        Group group = documentSnapshot.toObject(Group.class);
                        if (group != null && group.getMembers() != null && !group.getMembers().contains(currentUserId)) {
                            db.collection("groups").document(groupId)
                                    .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUserId));
                        }
                    }
                });
    }

    public void sendMessage(I.imessenger.models.ChatMessage message) {
        db.collection("chat").add(message);
    }

    public com.google.firebase.firestore.CollectionReference getChatCollection() {
        return db.collection("chat");
    }
}
