package i.imessenger.fragments;

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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.ConversationsAdapter;
import i.imessenger.databinding.FragmentChatListBinding;
import i.imessenger.models.ChatConversation;
import i.imessenger.models.Group;
import i.imessenger.models.User;
import i.imessenger.viewmodels.ChatListViewModel;

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

    private ChatListViewModel viewModel;
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

        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

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
        observeData();
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
            refreshAdapter();
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
        
        conversationsAdapter = new ConversationsAdapter(getContext(), filteredList);
        binding.recyclerViewUsers.setAdapter(conversationsAdapter);
    }

    private void observeData() {
        binding.setIsLoading(true);

        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUserModel = user;
                viewModel.ensureDefaultGroups(user);
                
                // Now observe other data
                observeGroupsAndUsers();
            } else {
                binding.setIsLoading(false);
                Toast.makeText(getContext(), "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeGroupsAndUsers() {
        // My Groups
        viewModel.getMyGroups().observe(getViewLifecycleOwner(), groups -> {
            groupsList.clear();
            if (groups != null) {
                for (Group group : groups) {
                    groupsList.add(new ChatConversation(group));
                }
            }
            if (!isDiscoverTab) refreshAdapter();
        });

        // Public Groups
        viewModel.getPublicGroups().observe(getViewLifecycleOwner(), groups -> {
            publicGroupsList.clear();
            if (groups != null) {
                for (Group group : groups) {
                    publicGroupsList.add(new ChatConversation(group));
                }
            }
            if (isDiscoverTab) refreshAdapter();
        });

        // Event Groups
        viewModel.getEventGroups().observe(getViewLifecycleOwner(), groups -> {
            eventGroupsList.clear();
            if (groups != null) {
                for (Group group : groups) {
                    eventGroupsList.add(new ChatConversation(group));
                }
            }
            if (isDiscoverTab) refreshAdapter();
        });

        // Users (Admins & Classmates)
        viewModel.getAllUsers().observe(getViewLifecycleOwner(), users -> {
            adminList.clear();
            classmatesList.clear();
            if (users != null && currentUserModel != null) {
                for (User u : users) {
                    if ("admin".equalsIgnoreCase(u.getRole())) {
                        adminList.add(new ChatConversation(u));
                    } else if (u.getLevel() != null && u.getLevel().equals(currentUserModel.getLevel())) {
                        classmatesList.add(new ChatConversation(u));
                    }
                }
            }
            if (!isDiscoverTab) refreshAdapter();
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

        conversationsAdapter = new ConversationsAdapter(getContext(), conversationList);
        binding.recyclerViewUsers.setAdapter(conversationsAdapter);
        
        binding.setIsLoading(false);
    }
}
