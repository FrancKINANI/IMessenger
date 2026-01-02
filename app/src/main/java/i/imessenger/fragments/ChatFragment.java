package i.imessenger.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.ChatAdapter;
import i.imessenger.adapters.SelectedMediaAdapter;
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

    // Media selection
    private List<Uri> selectedMediaUris = new ArrayList<>();
    private List<String> selectedMediaTypes = new ArrayList<>();
    private List<String> selectedMediaNames = new ArrayList<>();
    private SelectedMediaAdapter mediaAdapter;

    private final ActivityResultLauncher<String> requestImagePermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchImagePicker();
                } else {
                    Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> requestVideoPermission = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    launchVideoPicker();
                } else {
                    Toast.makeText(requireContext(), R.string.permission_denied, Toast.LENGTH_SHORT).show();
                }
            }
    );

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedMediaUris.add(uri);
                    selectedMediaTypes.add("image");
                    selectedMediaNames.add(getFileName(uri));
                    updateMediaPreview();
                }
            }
    );

    private final ActivityResultLauncher<String> pickVideo = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedMediaUris.add(uri);
                    selectedMediaTypes.add("video");
                    selectedMediaNames.add(getFileName(uri));
                    updateMediaPreview();
                }
            }
    );

    private final ActivityResultLauncher<String> pickDocument = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    selectedMediaUris.add(uri);
                    selectedMediaTypes.add("document");
                    selectedMediaNames.add(getFileName(uri));
                    updateMediaPreview();
                }
            }
    );

    private String getFileName(Uri uri) {
        String fileName = "document";
        if (uri != null && getContext() != null) {
            android.database.Cursor cursor = requireContext().getContentResolver()
                    .query(uri, null, null, null, null);
            if (cursor != null) {
                try {
                    if (cursor.moveToFirst()) {
                        int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if (nameIndex >= 0) {
                            fileName = cursor.getString(nameIndex);
                        }
                    }
                } finally {
                    cursor.close();
                }
            }
        }
        return fileName;
    }

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

        binding.setIsLoading(true);
        binding.setIsUploading(false);

        init();
        setupMediaSelection();

        chatViewModel.initChat(receiverUserId, groupId);
        
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null && binding != null) {
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

        chatViewModel.getIsUploading().observe(getViewLifecycleOwner(), isUploading -> {
            if (binding != null) {
                binding.setIsUploading(isUploading);
            }
        });

        chatViewModel.getUploadError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty() && getContext() != null) {
                Toast.makeText(requireContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
            }
        });

        if (groupId != null) {
            chatViewModel.getGroupInfo(groupId).observe(getViewLifecycleOwner(), group -> {
                if (binding == null) return;
                if (group != null && group.getBlockedMembers() != null && group.getBlockedMembers().contains(currentUserId)) {
                    binding.inputLayout.setVisibility(View.GONE);
                } else {
                    binding.inputLayout.setVisibility(View.VISIBLE);
                }
            });
        }

        binding.layoutSend.setOnClickListener(v -> sendMessage());
    }

    private void init() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages, currentUserId);
        binding.chatRecyclerView.setAdapter(chatAdapter);

        // Setup selected media preview
        mediaAdapter = new SelectedMediaAdapter(selectedMediaUris, selectedMediaTypes, position -> {
            selectedMediaUris.remove(position);
            selectedMediaTypes.remove(position);
            if (position < selectedMediaNames.size()) {
                selectedMediaNames.remove(position);
            }
            updateMediaPreview();
        });
        binding.selectedMediaRecyclerView.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.selectedMediaRecyclerView.setAdapter(mediaAdapter);
    }

    private void setupMediaSelection() {
        binding.btnAttachImage.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                        == PackageManager.PERMISSION_GRANTED) {
                    launchImagePicker();
                } else {
                    requestImagePermission.launch(Manifest.permission.READ_MEDIA_IMAGES);
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    launchImagePicker();
                } else {
                    requestImagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });

        binding.btnAttachVideo.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO)
                        == PackageManager.PERMISSION_GRANTED) {
                    launchVideoPicker();
                } else {
                    requestVideoPermission.launch(Manifest.permission.READ_MEDIA_VIDEO);
                }
            } else {
                if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                        == PackageManager.PERMISSION_GRANTED) {
                    launchVideoPicker();
                } else {
                    requestVideoPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
                }
            }
        });

        binding.btnAttachDocument.setOnClickListener(v -> {
            launchDocumentPicker();
        });
    }

    private void launchImagePicker() {
        pickImage.launch("image/*");
    }

    private void launchVideoPicker() {
        pickVideo.launch("video/*");
    }

    private void launchDocumentPicker() {
        pickDocument.launch("*/*");
    }

    private void updateMediaPreview() {
        if (binding == null) return;

        if (selectedMediaUris.isEmpty()) {
            binding.selectedMediaRecyclerView.setVisibility(View.GONE);
        } else {
            binding.selectedMediaRecyclerView.setVisibility(View.VISIBLE);
            mediaAdapter.notifyDataSetChanged();
        }
    }

    private void sendMessage() {
        if (binding == null) return;

        String messageText = binding.inputMessage.getText().toString().trim();

        if (messageText.isEmpty() && selectedMediaUris.isEmpty()) {
            return;
        }

        if (!selectedMediaUris.isEmpty()) {
            // Send with media
            chatViewModel.sendMediaMessage(
                messageText,
                new ArrayList<>(selectedMediaUris),
                new ArrayList<>(selectedMediaTypes),
                new ArrayList<>(selectedMediaNames),
                null, null, null
            );

            // Clear selection
            selectedMediaUris.clear();
            selectedMediaTypes.clear();
            selectedMediaNames.clear();
            updateMediaPreview();
        } else {
            // Text only message
            chatViewModel.sendMessage(
                messageText,
                null, null, null
            );
        }

        binding.inputMessage.setText(null);
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
