package i.imessenger.activities;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.ChatAdapter;
import i.imessenger.databinding.ActivityChatBinding;
import i.imessenger.models.ChatMessage;
import i.imessenger.viewmodels.ChatViewModel;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;
    private String currentUserId;
    private String receiverUserId;
    private String groupId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiverUserId = getIntent().getStringExtra("userId");
        groupId = getIntent().getStringExtra("groupId");
        String userName = getIntent().getStringExtra("userName");
        String userImage = getIntent().getStringExtra("userImage");
        currentUserId = FirebaseAuth.getInstance().getUid();

        chatViewModel = new ViewModelProvider(this).get(ChatViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (userName != null) {
            binding.setReceiverName(userName);
        }
        
        binding.setReceiverImage(userImage);
        // binding.setViewModel(chatViewModel);
        binding.setIsLoading(true);

        init();
        
        chatViewModel.initChat(receiverUserId, groupId);
        
        chatViewModel.getMessages().observe(this, messages -> {
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
            chatViewModel.getGroupInfo(groupId).observe(this, group -> {
                if (group != null && group.getBlockedMembers() != null && group.getBlockedMembers().contains(currentUserId)) {
                    binding.layoutSend.setVisibility(View.GONE);
                    // Optionally show a toast or a message saying "You are blocked"
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
            null, null, null // Conversion info not strictly needed for basic chat
        );
        
        binding.inputMessage.setText(null);
    }
}
