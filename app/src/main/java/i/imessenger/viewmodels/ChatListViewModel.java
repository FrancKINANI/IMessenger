package i.imessenger.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import i.imessenger.models.ChatConversation;
import i.imessenger.models.Group;
import i.imessenger.models.User;
import i.imessenger.repositories.ChatRepository;

public class ChatListViewModel extends AndroidViewModel {

    private final ChatRepository chatRepository;
    private final MutableLiveData<List<ChatConversation>> conversationsLiveData = new MutableLiveData<>();
    private final MutableLiveData<List<ChatConversation>> discoverLiveData = new MutableLiveData<>();

    public ChatListViewModel(@NonNull Application application) {
        super(application);
        chatRepository = new ChatRepository(application.getApplicationContext());
        loadData();
    }

    public LiveData<List<ChatConversation>> getConversations() {
        return conversationsLiveData;
    }

    public LiveData<List<ChatConversation>> getDiscoverItems() {
        return discoverLiveData;
    }

    public void loadData() {
        chatRepository.getCurrentUser().observeForever(user -> {
            if (user != null) {
                fetchConversations(user);
                fetchDiscoverItems();
            }
        });
    }

    private void fetchDiscoverItems() {
        // Fetch users or groups for the discover section
        // This is a placeholder implementation.
        // You should implement the logic to fetch discover items.
        List<ChatConversation> discoverList = new ArrayList<>();
        discoverLiveData.setValue(discoverList);
    }

    private void fetchConversations(User user) {
        List<ChatConversation> conversationList = new ArrayList<>();

        // 1. My Groups
        ensureDefaultGroups(user);
        
        // We need to fetch groups where user is member. 
        // Since Firestore queries are async, we might need a way to combine results.
        // For simplicity in this migration, we'll fetch all users and filter, 
        // but ideally we should have specific queries.
        
        // Fetching all users for "Classmates" and "Administration"
        chatRepository.getUsers().observeForever(users -> {
            if (users != null) {
                List<User> admins = new ArrayList<>();
                List<User> classmates = new ArrayList<>();

                for (User u : users) {
                    if ("admin".equalsIgnoreCase(u.getRole())) {
                        admins.add(u);
                    } else if (u.getLevel() != null && u.getLevel().equals(user.getLevel())) {
                        classmates.add(u);
                    }
                }

                // We also need to fetch the groups the user is in.
                // This is getting tricky with multiple async calls.
                // For now, let's assume we fetch groups separately and combine.
                // But `getGroups` returns LiveData.
                
                // Let's try to structure this better.
                // We will just expose the raw lists and let the Fragment combine them?
                // Or use a MediatorLiveData.
                
                // For this step, I'll implement a simpler approach:
                // Just trigger the fetches and update the main list when they return.
                // This might cause multiple updates but it's safer.
                
                updateConversationList(conversationList, admins, classmates);
            }
        });
    }

    private void updateConversationList(List<ChatConversation> list, List<User> admins, List<User> classmates) {
        // This method would need to be synchronized or handle partial updates.
        // Given the complexity of the original ChatListFragment logic, 
        // maybe it's better to keep some logic in the Fragment or move it all here.
        
        // Let's try to replicate the Fragment logic here but cleanly.
        
        // Groups (we need to fetch them)
        // ...
        
        // For now, I will just expose the raw data sources and let the Fragment build the list.
        // It's a valid MVVM pattern to have the ViewModel provide data and the View (Fragment) map it to UI models.
    }
    
    // Better approach: Expose specific data sets
    public LiveData<User> getCurrentUser() {
        return chatRepository.getCurrentUser();
    }
    
    public LiveData<List<User>> getAllUsers() {
        return chatRepository.getUsers();
    }
    
    public LiveData<List<Group>> getMyGroups() {
        return chatRepository.getMyGroups();
    }
    
    public LiveData<List<Group>> getPublicGroups() {
        return chatRepository.getPublicGroups();
    }
    
    public LiveData<List<Group>> getEventGroups() {
        return chatRepository.getEventGroups();
    }

    public void ensureDefaultGroups(User user) {
        if (user == null) return;

        // Class Group
        if (user.getLevel() != null && !user.getLevel().isEmpty()) {
            String classGroupId = "class_" + user.getLevel().replaceAll("\\s+", "");
            ensureGroupExists(classGroupId, "Class " + user.getLevel(), "CLASS", Collections.singletonList(user.getUid()), new ArrayList<String>());
        }

        // Club Groups
        if (user.getGroups() != null && !user.getGroups().isEmpty()) {
            String[] clubs = user.getGroups().split(",");
            for (String club : clubs) {
                String clubName = club.trim();
                if (!clubName.isEmpty()) {
                    String clubGroupId = "club_" + clubName.replaceAll("\\s+", "");
                    ensureGroupExists(clubGroupId, clubName + " Club", "CLUB", Collections.singletonList(user.getUid()), new ArrayList<String>());
                }
            }
        }

        // Alumni Group
        if ("alumni".equalsIgnoreCase(user.getRole())) {
            ensureGroupExists("alumni_general", "Alumni General", "ALUMNI", Collections.singletonList(user.getUid()), new ArrayList<String>());
        }
        
        // Public Group
        ensureGroupExists("public_general", "School General", "PUBLIC", Collections.singletonList(user.getUid()), new ArrayList<String>());
    }

    private void ensureGroupExists(String groupId, String groupName, String groupType, List<String> members, List<String> admins) {
        chatRepository.ensureGroupExists(groupId, groupName, groupType, members, admins);
    }
}
