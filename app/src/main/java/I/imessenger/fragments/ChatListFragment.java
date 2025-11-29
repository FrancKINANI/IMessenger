package I.imessenger.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

import I.imessenger.R;
import I.imessenger.adapters.ConversationsAdapter;
import I.imessenger.databinding.FragmentChatListBinding;
import I.imessenger.models.ChatConversation;
import I.imessenger.models.Group;
import I.imessenger.models.User;

public class ChatListFragment extends Fragment {

    private FragmentChatListBinding binding;
    private ConversationsAdapter conversationsAdapter;
    private List<ChatConversation> conversationList;
    
    // Section Lists
    private List<ChatConversation> groupsList;
    private List<ChatConversation> adminList;
    private List<ChatConversation> classmatesList;
    
    // Discover Lists
    private List<ChatConversation> publicGroupsList;
    private List<ChatConversation> eventGroupsList;

    private FirebaseFirestore db;
    private User currentUserModel;
    
    private boolean isDiscoverTab = false;

    public ChatListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        conversationList = new ArrayList<>();
        groupsList = new ArrayList<>();
        adminList = new ArrayList<>();
        classmatesList = new ArrayList<>();
        publicGroupsList = new ArrayList<>();
        eventGroupsList = new ArrayList<>();

        conversationsAdapter = new ConversationsAdapter(getContext(), conversationList);

        binding.recyclerViewUsers.setHasFixedSize(true);
        binding.recyclerViewUsers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewUsers.setAdapter(conversationsAdapter);

        setupTabs();
        setupSearch();
        loadData();
    }

    private void setupTabs() {
        binding.tabChats.setOnClickListener(v -> {
            isDiscoverTab = false;
            updateTabUI();
            refreshAdapter();
        });

        binding.tabDiscover.setOnClickListener(v -> {
            isDiscoverTab = true;
            updateTabUI();
            fetchDiscoverGroups(); // Fetch when tab clicked
        });
    }

    private void updateTabUI() {
        if (isDiscoverTab) {
            binding.tabChats.setBackground(null);
            binding.tabChats.setAlpha(0.7f);
            binding.tabDiscover.setBackgroundResource(R.drawable.tab_indicator_selected);
            binding.tabDiscover.setAlpha(1.0f);
        } else {
            binding.tabChats.setBackgroundResource(R.drawable.tab_indicator_selected);
            binding.tabChats.setAlpha(1.0f);
            binding.tabDiscover.setBackground(null);
            binding.tabDiscover.setAlpha(0.7f);
        }
    }

    private void setupSearch() {
        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().isEmpty()) {
                    conversationsAdapter.setSearchMode(false);
                    refreshAdapter();
                } else {
                    conversationsAdapter.setSearchMode(true);
                    filter(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filter(String query) {
        // Simple client-side filtering for now
        List<ChatConversation> filteredList = new ArrayList<>();
        List<ChatConversation> sourceList = new ArrayList<>();
        
        if (isDiscoverTab) {
            sourceList.addAll(publicGroupsList);
            sourceList.addAll(eventGroupsList);
        } else {
            sourceList.addAll(groupsList);
            sourceList.addAll(adminList);
            sourceList.addAll(classmatesList);
        }

        for (ChatConversation item : sourceList) {
            if (item.getName() != null && item.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(item);
            }
        }
        
        // Re-build adapter list with headers if needed, or just show flat list for search
        // For simplicity, just show flat list
        conversationsAdapter = new ConversationsAdapter(getContext(), filteredList);
        binding.recyclerViewUsers.setAdapter(conversationsAdapter);
    }

    private void loadData() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser == null) return;

        binding.progressBar.setVisibility(View.VISIBLE);

        // 1. Get Current User Details
        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    currentUserModel = documentSnapshot.toObject(User.class);
                    if (currentUserModel != null) {
                        fetchGroupsAndContacts();
                    } else {
                        binding.progressBar.setVisibility(View.GONE);
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchGroupsAndContacts() {
        groupsList.clear();
        adminList.clear();
        classmatesList.clear();

        // A. Fetch/Ensure Groups
        // Class Group
        if (currentUserModel.getLevel() != null && !currentUserModel.getLevel().isEmpty()) {
            String classGroupId = "class_" + currentUserModel.getLevel().replaceAll("\\s+", "");
            ensureGroupExists(classGroupId, "Class " + currentUserModel.getLevel(), "CLASS");
        }

        // Club Groups
        if (currentUserModel.getGroups() != null && !currentUserModel.getGroups().isEmpty()) {
            String[] clubs = currentUserModel.getGroups().split(",");
            for (String club : clubs) {
                String clubName = club.trim();
                if (!clubName.isEmpty()) {
                    String clubGroupId = "club_" + clubName.replaceAll("\\s+", "");
                    ensureGroupExists(clubGroupId, clubName + " Club", "CLUB");
                }
            }
        }

        // Alumni Group
        if ("alumni".equalsIgnoreCase(currentUserModel.getRole())) {
            ensureGroupExists("alumni_general", "Alumni General", "ALUMNI");
        }

        // B. Fetch Admins
        db.collection("users")
                .whereEqualTo("role", "admin")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        User user = doc.toObject(User.class);
                        if (!user.getUid().equals(currentUserModel.getUid())) {
                            adminList.add(new ChatConversation(user));
                        }
                    }
                    if (!isDiscoverTab) refreshAdapter();
                });

        // C. Fetch Classmates
        if (currentUserModel.getLevel() != null) {
            db.collection("users")
                    .whereEqualTo("level", currentUserModel.getLevel())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            User user = doc.toObject(User.class);
                            // Exclude self and admins (already in admin list)
                            if (!user.getUid().equals(currentUserModel.getUid()) && !"admin".equals(user.getRole())) {
                                classmatesList.add(new ChatConversation(user));
                            }
                        }
                        if (!isDiscoverTab) refreshAdapter();
                    });
        }
    }

    private void fetchDiscoverGroups() {
        binding.progressBar.setVisibility(View.VISIBLE);
        publicGroupsList.clear();
        eventGroupsList.clear();

        // Ensure at least one public group exists
        ensureGroupExists("public_general", "School General", "PUBLIC");

        // Fetch Public Groups
        db.collection("groups")
                .whereEqualTo("groupType", "PUBLIC")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        Group group = doc.toObject(Group.class);
                        publicGroupsList.add(new ChatConversation(group));
                    }
                    
                    // Fetch Event Groups
                    db.collection("groups")
                            .whereEqualTo("groupType", "EVENT")
                            .get()
                            .addOnSuccessListener(eventSnapshots -> {
                                for (QueryDocumentSnapshot doc : eventSnapshots) {
                                    Group group = doc.toObject(Group.class);
                                    eventGroupsList.add(new ChatConversation(group));
                                }
                                if (isDiscoverTab) refreshAdapter();
                            });
                });
    }

    private void ensureGroupExists(String groupId, String groupName, String type) {
        db.collection("groups").document(groupId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    Group group;
                    if (documentSnapshot.exists()) {
                        group = documentSnapshot.toObject(Group.class);
                    } else {
                        // Create Group
                        List<String> members = new ArrayList<>();
                        members.add(currentUserModel.getUid());
                        List<String> admins = new ArrayList<>();
                        
                        group = new Group(groupId, groupName, null, type, members, admins);
                        db.collection("groups").document(groupId).set(group);
                    }
                    
                    if (group != null) {
                        boolean exists = false;
                        List<ChatConversation> targetList;
                        
                        if ("PUBLIC".equals(type)) {
                            targetList = publicGroupsList;
                        } else if ("EVENT".equals(type)) {
                            targetList = eventGroupsList;
                        } else {
                            targetList = groupsList;
                        }

                        for(ChatConversation c : targetList) {
                            if(c.getId().equals(group.getGroupId())) exists = true;
                        }
                        
                        if(!exists) targetList.add(new ChatConversation(group));
                        
                        if (isDiscoverTab && ("PUBLIC".equals(type) || "EVENT".equals(type))) {
                            refreshAdapter();
                        } else if (!isDiscoverTab && !("PUBLIC".equals(type) || "EVENT".equals(type))) {
                            refreshAdapter();
                        }
                    }
                });
    }

    private synchronized void refreshAdapter() {
        conversationList.clear();

        if (isDiscoverTab) {
             if (!publicGroupsList.isEmpty()) {
                conversationList.add(new ChatConversation("Public Channels"));
                conversationList.addAll(publicGroupsList);
            }
            if (!eventGroupsList.isEmpty()) {
                conversationList.add(new ChatConversation("Events"));
                conversationList.addAll(eventGroupsList);
            }
            if (publicGroupsList.isEmpty() && eventGroupsList.isEmpty()) {
                // Maybe show empty state or "No public groups found"
            }
        } else {
            if (!groupsList.isEmpty()) {
                conversationList.add(new ChatConversation("My Groups"));
                conversationList.addAll(groupsList);
            }

            if (!adminList.isEmpty()) {
                conversationList.add(new ChatConversation("Administration"));
                conversationList.addAll(adminList);
            }

            if (!classmatesList.isEmpty()) {
                conversationList.add(new ChatConversation("Classmates"));
                conversationList.addAll(classmatesList);
            }
        }

        // Reset adapter to original list (in case search filter was active)
        conversationsAdapter = new ConversationsAdapter(getContext(), conversationList);
        binding.recyclerViewUsers.setAdapter(conversationsAdapter);
        
        binding.progressBar.setVisibility(View.GONE);
    }
}
