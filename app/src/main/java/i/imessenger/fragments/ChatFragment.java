package i.imessenger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.ChatAdapter;
import i.imessenger.databinding.FragmentChatBinding;
import i.imessenger.models.ChatMessage;
import i.imessenger.viewmodels.ChatViewModel;

public class ChatFragment extends Fragment {

    private FragmentChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;
    private String currentUserId;
    private String receiverUserId;
    private String groupId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChatBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            receiverUserId = getArguments().getString("userId");
            groupId = getArguments().getString("groupId");
            String userName = getArguments().getString("userName");
            String userImage = getArguments().getString("userImage");

            if (userName != null) {
                binding.setReceiverName(userName);
            }
            binding.setReceiverImage(userImage);
        }
        
        currentUserId = FirebaseAuth.getInstance().getUid();
        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
        
        binding.headerContent.setOnClickListener(v -> {
            if (receiverUserId != null) {
                Bundle bundle = new Bundle();
                bundle.putString("userId", receiverUserId);
                NavHostFragment.findNavController(this).navigate(R.id.userProfileFragment, bundle);
            }
        });

        // binding.setViewModel(chatViewModel); // Uncomment if binding layout supports it
        binding.setIsLoading(true);

        init();
        
        chatViewModel.initChat(receiverUserId, groupId);
        
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null) {
                int count = chatMessages.size();
                chatMessages.clear();
                chatMessages.addAll(messages);
                chatAdapter.notifyDataSetChanged();
                
                if (chatMessages.size() > count) {
                    binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
                }
                binding.setIsLoading(false);
            }
        });

        if (groupId != null) {
            chatViewModel.getGroupInfo(groupId).observe(getViewLifecycleOwner(), group -> {
                if (group != null && group.getBlockedMembers() != null && group.getBlockedMembers().contains(currentUserId)) {
                    binding.layoutSend.setVisibility(View.GONE);
                } else {
                    binding.layoutSend.setVisibility(View.VISIBLE);
                }
            });
        }

        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        binding.chatRecyclerView.setAdapter(chatAdapter);
    }

    private void sendMessage() {
        if (binding.inputMessage.getText().toString().trim().isEmpty()) {
            return;
        }

        chatViewModel.sendMessage(
            binding.inputMessage.getText().toString(),
            null, null, null
        );
        
        binding.inputMessage.setText(null);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
