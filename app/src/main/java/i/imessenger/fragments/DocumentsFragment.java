package i.imessenger.fragments;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.Chip;

import i.imessenger.R;
import i.imessenger.adapters.DocumentAdapter;
import i.imessenger.databinding.FragmentDocumentsBinding;
import i.imessenger.models.DocumentFile;
import i.imessenger.viewmodels.DocumentViewModel;

public class DocumentsFragment extends Fragment {

    private FragmentDocumentsBinding binding;
    private DocumentViewModel viewModel;
    private DocumentAdapter adapter;
    private String userLevel = ""; // Identify user level/role if needed, fetch from VM

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri fileUri = result.getData().getData();
                    if (fileUri != null) {
                        uploadFile(fileUri);
                    }
                }
            });

    public DocumentsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentDocumentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(DocumentViewModel.class);

        setupRecyclerView();
        setupToolbar();
        setupTabs();
        setupFab();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new DocumentAdapter(new DocumentAdapter.OnDocumentClickListener() {
            @Override
            public void onDownloadClick(DocumentFile file) {
                // Open URL in browser for now
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(file.getUrl()));
                startActivity(browserIntent);
            }

            @Override
            public void onDeleteClick(DocumentFile file) {
                viewModel.deleteDocument(file);
                Toast.makeText(getContext(), "Document deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onItemClick(DocumentFile file) {
                // Same as download for now or open viewer
                onDownloadClick(file);
            }
        });
        binding.recyclerViewDocuments.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewDocuments.setAdapter(adapter);
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupTabs() {
        binding.chipGroupFolders.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId != -1) {
                Chip chip = group.findViewById(checkedId);
                String folder = chip.getText().toString();
                viewModel.setFolder(folder);
                refreshList();
            }
        });
    }

    private void setupFab() {
        binding.fabUpload.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            String[] mimetypes = { "application/pdf", "application/msword", "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain",
                    "image/*" };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimetypes);
            filePickerLauncher.launch(intent);
        });
    }

    private void uploadFile(Uri fileUri) {
        // Simplified upload dialog or prompt for name
        // For MVP, use filename from URI or default
        String name = "New Document"; // Should get real name
        String type = "unknown";

        // Show loading?
        Toast.makeText(getContext(), "Uploading...", Toast.LENGTH_SHORT).show();

        viewModel.uploadDocument(name, fileUri, type, userLevel).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(getContext(), "Upload successful", Toast.LENGTH_SHORT).show();
                refreshList();
            } else {
                Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeData() {
        viewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                userLevel = user.getLevel();
                refreshList();
            }
        });
    }

    private void refreshList() {
        viewModel.getDocuments(userLevel).observe(getViewLifecycleOwner(), documents -> {
            adapter.setDocuments(documents);
            binding.emptyView.setVisibility(documents.isEmpty() ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
