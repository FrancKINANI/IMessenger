package i.imessenger.fragments;

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
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.MediaGridAdapter;
import i.imessenger.databinding.FragmentMediaGalleryBinding;
import i.imessenger.models.MediaItem;
import i.imessenger.viewmodels.MediaViewModel;

public class MediaGalleryFragment extends Fragment {

    private FragmentMediaGalleryBinding binding;
    private MediaViewModel mediaViewModel;
    private String userId;
    private boolean isCurrentUser;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadMedia(uri, "image");
                }
            }
    );

    private final ActivityResultLauncher<String> pickVideo = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    uploadMedia(uri, "video");
                }
            }
    );

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMediaGalleryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);
        isCurrentUser = userId != null && userId.equals(mediaViewModel.getCurrentUserId());

        setupToolbar();
        setupTabs();
        setupFab();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void setupTabs() {
        MediaPagerAdapter pagerAdapter = new MediaPagerAdapter(this, userId);
        binding.viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText(R.string.all_media);
                    break;
                case 1:
                    tab.setText(R.string.images);
                    break;
                case 2:
                    tab.setText(R.string.videos);
                    break;
            }
        }).attach();
    }

    private void setupFab() {
        if (isCurrentUser) {
            binding.fabUpload.setVisibility(View.VISIBLE);
            binding.fabUpload.setOnClickListener(v -> showUploadOptions());
        } else {
            binding.fabUpload.setVisibility(View.GONE);
        }
    }

    private void showUploadOptions() {
        String[] options = {getString(R.string.photo), getString(R.string.video)};

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.upload_image)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        pickImage.launch("image/*");
                    } else {
                        pickVideo.launch("video/*");
                    }
                })
                .show();
    }

    private void uploadMedia(android.net.Uri uri, String type) {
        Toast.makeText(requireContext(), R.string.uploading, Toast.LENGTH_SHORT).show();

        MediaViewModel.UploadCompleteCallback callback = (success, mediaItem) -> {
            if (success) {
                Toast.makeText(requireContext(), R.string.upload_complete, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.upload_failed, Toast.LENGTH_SHORT).show();
            }
        };

        if ("video".equals(type)) {
            mediaViewModel.uploadVideo(uri, "", callback);
        } else {
            mediaViewModel.uploadImage(uri, "", callback);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    // Inner Pager Adapter
    public static class MediaPagerAdapter extends androidx.viewpager2.adapter.FragmentStateAdapter {
        private String userId;

        public MediaPagerAdapter(Fragment fragment, String userId) {
            super(fragment);
            this.userId = userId;
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            MediaListFragment fragment = new MediaListFragment();
            Bundle args = new Bundle();
            args.putString("userId", userId);
            args.putInt("filterType", position); // 0=all, 1=images, 2=videos
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }
}

