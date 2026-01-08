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
    private final androidx.lifecycle.MediatorLiveData<List<ChatConversation>> conversationsLiveData = new androidx.lifecycle.MediatorLiveData<>();
    private final MutableLiveData<List<ChatConversation>> discoverLiveData = new MutableLiveData<>();
    private User currentUser;

    public ChatListViewModel(@NonNull Application application) {
        super(application);
        chatRepository = ChatRepository.getInstance(application.getApplicationContext());
        setupConversationsSource();
    }

    private void setupConversationsSource() {
        LiveData<User> userSource = chatRepository.getCurrentUser();
        LiveData<List<Group>> groupsSource = chatRepository.getMyGroups();
        LiveData<List<User>> usersSource = chatRepository.getUsers();

        conversationsLiveData.addSource(userSource, user -> {
            currentUser = user;
            ensureDefaultGroups(user);
            combineData(groupsSource.getValue(), usersSource.getValue());
        });
        conversationsLiveData.addSource(groupsSource, groups -> combineData(groups, usersSource.getValue()));
        conversationsLiveData.addSource(usersSource, users -> combineData(groupsSource.getValue(), users));
    }

    private void combineData(List<Group> groups, List<User> users) {
        if (currentUser == null)
            return;
        List<ChatConversation> list = new ArrayList<>();

        // Add Groups
        if (groups != null) {
            for (Group group : groups) {
                list.add(new ChatConversation(group));
            }
        }

        // Add Classmates / Admins (Users)
        if (users != null) {
            for (User u : users) {
                if ("admin".equalsIgnoreCase(u.getRole())) {
                    list.add(new ChatConversation(u));
                } else if (u.getLevel() != null && u.getLevel().equals(currentUser.getLevel())
                        && !u.getUid().equals(currentUser.getUid())) {
                    list.add(new ChatConversation(u));
                }
            }
        }
        conversationsLiveData.setValue(list);
    }

    public LiveData<List<ChatConversation>> getConversations() {
        return conversationsLiveData;
    }

    public LiveData<List<ChatConversation>> getDiscoverItems() {
        return discoverLiveData; // Kept empty for now
    }

    public void loadData() {
        // Data loading is handled by active observations
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
        if (user == null)
            return;

        // Class Group
        if (user.getLevel() != null && !user.getLevel().isEmpty()) {
            String classGroupId = "class_" + user.getLevel().replaceAll("\\s+", "");
            ensureGroupExists(classGroupId, "Class " + user.getLevel(), "CLASS",
                    Collections.singletonList(user.getUid()), new ArrayList<String>());
        }

        // Club Groups
        if (user.getGroups() != null && !user.getGroups().isEmpty()) {
            String[] clubs = user.getGroups().split(",");
            for (String club : clubs) {
                String clubName = club.trim();
                if (!clubName.isEmpty()) {
                    String clubGroupId = "club_" + clubName.replaceAll("\\s+", "");
                    ensureGroupExists(clubGroupId, clubName + " Club", "CLUB", Collections.singletonList(user.getUid()),
                            new ArrayList<String>());
                }
            }
        }

        // Alumni Group
        if ("alumni".equalsIgnoreCase(user.getRole())) {
            ensureGroupExists("alumni_general", "Alumni General", "ALUMNI", Collections.singletonList(user.getUid()),
                    new ArrayList<String>());
        }

        // Public Group
        ensureGroupExists("public_general", "School General", "PUBLIC", Collections.singletonList(user.getUid()),
                new ArrayList<String>());
    }

    private void ensureGroupExists(String groupId, String groupName, String groupType, List<String> members,
            List<String> admins) {
        chatRepository.ensureGroupExists(groupId, groupName, groupType, members, admins);
    }
}
