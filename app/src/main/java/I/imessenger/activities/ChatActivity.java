package I.imessenger.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.bumptech.glide.Glide;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

import I.imessenger.R;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import I.imessenger.adapters.ChatAdapter;
import I.imessenger.databinding.ActivityChatBinding;
import I.imessenger.models.ChatMessage;

public class ChatActivity extends AppCompatActivity {

    private ActivityChatBinding binding;
    private List<ChatMessage> chatMessages;
    private ChatAdapter chatAdapter;
    private FirebaseFirestore database;
    private String currentUserId;
    private String receiverUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        receiverUserId = getIntent().getStringExtra("userId");
        String userName = getIntent().getStringExtra("userName");
        String userImage = getIntent().getStringExtra("userImage");
        currentUserId = FirebaseAuth.getInstance().getUid();

        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Disable default title
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        if (userName != null) {
            binding.textName.setText(userName);
        }

        if (userImage != null && !userImage.isEmpty()) {
            Glide.with(this)
                    .load(userImage)
                    .placeholder(R.drawable.logo)
                    .into(binding.imageProfile);
        } else {
            binding.imageProfile.setImageResource(R.drawable.logo);
        }

        init();
        listenMessages();

        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        binding.chatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        if (binding.inputMessage.getText().toString().trim().isEmpty()) {
            return;
        }

        HashMap<String, Object> message = new HashMap<>();
        message.put("senderId", currentUserId);
        message.put("receiverId", receiverUserId);
        message.put("message", binding.inputMessage.getText().toString());
        message.put("timestamp", new Date());

        database.collection("chat").add(message);
        binding.inputMessage.setText(null);
    }

    private void listenMessages() {
        database.collection("chat")
                .whereEqualTo("senderId", currentUserId)
                .whereEqualTo("receiverId", receiverUserId)
                .addSnapshotListener(eventListener);
        database.collection("chat")
                .whereEqualTo("senderId", receiverUserId)
                .whereEqualTo("receiverId", currentUserId)
                .addSnapshotListener(eventListener);
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString("senderId");
                    chatMessage.receiverId = documentChange.getDocument().getString("receiverId");
                    chatMessage.message = documentChange.getDocument().getString("message");
                    chatMessage.dateObject = documentChange.getDocument().getDate("timestamp");
                    chatMessage.dateTime = getReadableDateTime(chatMessage.dateObject);
                    chatMessages.add(chatMessage);
                }
            }
            Collections.sort(chatMessages, (obj1, obj2) -> obj1.dateObject.compareTo(obj2.dateObject));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
            binding.chatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
    };

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }
}
