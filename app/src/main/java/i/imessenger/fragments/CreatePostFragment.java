package i.imessenger.fragments;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.SelectedMediaAdapter;
import i.imessenger.databinding.DialogCreatePostBinding;
import i.imessenger.viewmodels.FeedViewModel;

public class CreatePostFragment extends Fragment {

    private DialogCreatePostBinding binding;
    private FeedViewModel feedViewModel;
    private List<Uri> selectedMediaUris = new ArrayList<>();
    private List<String> selectedMediaTypes = new ArrayList<>();
    private List<String> selectedMediaNames = new ArrayList<>();
    private SelectedMediaAdapter mediaAdapter;
    private boolean pendingImagePick = false;
    private boolean pendingVideoPick = false;

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
        String fileName = "file";
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
        binding = DialogCreatePostBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        setupToolbar();
        setupVisibilitySpinner();
        setupMediaRecyclerView();
        setupClickListeners();
        loadUserInfo();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void setupVisibilitySpinner() {
        String[] visibilityOptions = {
                getString(R.string.public_post),
                getString(R.string.class_only),
                getString(R.string.private_post)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_dropdown_item, visibilityOptions);
        binding.spinnerVisibility.setAdapter(adapter);
    }

    private void setupMediaRecyclerView() {
        mediaAdapter = new SelectedMediaAdapter(selectedMediaUris, selectedMediaTypes, position -> {
            selectedMediaUris.remove(position);
            selectedMediaTypes.remove(position);
            if (position < selectedMediaNames.size()) {
                selectedMediaNames.remove(position);
            }
            updateMediaPreview();
        });
        binding.recyclerViewMedia.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewMedia.setAdapter(mediaAdapter);
    }

    private void setupClickListeners() {
        binding.btnAddImage.setOnClickListener(v -> checkAndPickImage());
        binding.btnAddVideo.setOnClickListener(v -> checkAndPickVideo());
        binding.btnAddDocument.setOnClickListener(v -> launchDocumentPicker());
        binding.btnPost.setOnClickListener(v -> createPost());
    }

    private void checkAndPickImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_IMAGES)
                    == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                requestImagePermission.launch(Manifest.permission.READ_MEDIA_IMAGES);
            }
        } else {
            // Below Android 13 uses READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                launchImagePicker();
            } else {
                requestImagePermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
    }

    private void checkAndPickVideo() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_VIDEO
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_MEDIA_VIDEO)
                    == PackageManager.PERMISSION_GRANTED) {
                launchVideoPicker();
            } else {
                requestVideoPermission.launch(Manifest.permission.READ_MEDIA_VIDEO);
            }
        } else {
            // Below Android 13 uses READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                launchVideoPicker();
            } else {
                requestVideoPermission.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
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

    private void loadUserInfo() {
        feedViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                binding.tvUserName.setText(user.getFullName());
                if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                    Glide.with(this)
                            .load(user.getProfileImage())
                            .placeholder(R.drawable.logo)
                            .circleCrop()
                            .into(binding.ivUserAvatar);
                }
            }
        });
    }

    private void updateMediaPreview() {
        if (selectedMediaUris.isEmpty()) {
            binding.tvMediaLabel.setVisibility(View.GONE);
            binding.recyclerViewMedia.setVisibility(View.GONE);
        } else {
            binding.tvMediaLabel.setVisibility(View.VISIBLE);
            binding.recyclerViewMedia.setVisibility(View.VISIBLE);
            mediaAdapter.updateMedia(selectedMediaUris);
        }
    }

    private void createPost() {
        String content = binding.etPostContent.getText().toString().trim();

        if (content.isEmpty() && selectedMediaUris.isEmpty()) {
            Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
            return;
        }

        String visibility = getSelectedVisibility();

        binding.loadingOverlay.setVisibility(View.VISIBLE);
        binding.btnPost.setEnabled(false);

        feedViewModel.createPost(content, selectedMediaUris, selectedMediaTypes, selectedMediaNames, visibility, null)
                .observe(getViewLifecycleOwner(), success -> {
                    binding.loadingOverlay.setVisibility(View.GONE);
                    binding.btnPost.setEnabled(true);

                    if (success) {
                        Toast.makeText(requireContext(), R.string.post_created, Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this).navigateUp();
                    } else {
                        Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private String getSelectedVisibility() {
        int position = binding.spinnerVisibility.getSelectedItemPosition();
        switch (position) {
            case 1: return "class";
            case 2: return "private";
            default: return "public";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

