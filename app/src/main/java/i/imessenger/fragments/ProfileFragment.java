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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.MediaThumbnailAdapter;
import i.imessenger.adapters.PostPreviewAdapter;
import i.imessenger.databinding.FragmentProfileBinding;
import i.imessenger.models.FeedPost;
import i.imessenger.models.MediaItem;
import i.imessenger.models.User;
import i.imessenger.viewmodels.FeedViewModel;
import i.imessenger.viewmodels.MediaViewModel;
import i.imessenger.viewmodels.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private FeedViewModel feedViewModel;
    private MediaViewModel mediaViewModel;

    private MediaThumbnailAdapter mediaAdapter;
    private PostPreviewAdapter postAdapter;

    private List<MediaItem> mediaItems = new ArrayList<>();
    private List<FeedPost> posts = new ArrayList<>();

    private User currentUser;
    private boolean isUserDataLoaded = false;

    private final ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    binding.ivProfileImage.setImageURI(uri);
                    uploadProfileImage(uri);
                }
            });

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);
        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        setupAdapters();
        setupClickListeners();
        loadUserProfile();
    }

    private void setupAdapters() {
        // Media adapter
        mediaAdapter = new MediaThumbnailAdapter(requireContext(), mediaItems,
                new MediaThumbnailAdapter.OnMediaClickListener() {
                    @Override
                    public void onMediaClicked(MediaItem mediaItem, int position) {
                        openMediaViewer(mediaItem);
                    }

                    @Override
                    public void onMediaLongClicked(MediaItem mediaItem, int position) {
                        // Could show delete option
                    }
                });
        binding.recyclerViewMedia.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerViewMedia.setAdapter(mediaAdapter);

        // Posts adapter
        postAdapter = new PostPreviewAdapter(requireContext(), posts, post -> {
            // Navigate to full post view
            Bundle args = new Bundle();
            args.putString("postId", post.getPostId());
            NavHostFragment.findNavController(this).navigate(R.id.commentsFragment, args);
        });
        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPosts.setAdapter(postAdapter);
    }

    private void setupClickListeners() {
        binding.btnSettings.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.settingsFragment);
        });

        binding.ivEditImage.setOnClickListener(v -> pickImage.launch("image/*"));
        binding.ivProfileImage.setOnClickListener(v -> pickImage.launch("image/*"));

        binding.btnEditProfile.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.editProfileFragment);
        });

        binding.tvViewAllMedia.setOnClickListener(v -> {
            // Navigate to full media gallery
            if (currentUser != null) {
                Bundle args = new Bundle();
                args.putString("userId", currentUser.getUid());
                NavHostFragment.findNavController(this).navigate(R.id.mediaGalleryFragment, args);
            }
        });

        binding.tvViewAllPosts.setOnClickListener(v -> {
            // Navigate to all posts
            if (currentUser != null) {
                Bundle args = new Bundle();
                args.putString("userId", currentUser.getUid());
                NavHostFragment.findNavController(this).navigate(R.id.userPostsFragment, args);
            }
        });
    }

    private void loadUserProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);

        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                binding.progressBar.setVisibility(View.GONE);

                if (user != null) {
                    currentUser = user;
                    populateUserData(user);
                    // Only load posts and media once to avoid multiple observers
                    if (!isUserDataLoaded) {
                        isUserDataLoaded = true;
                        loadUserMedia(user.getUid());
                        loadUserPosts(user.getUid());
                    }
                } else {
                    createMissingUserDocument(firebaseUser);
                }
            });
        }
    }

    private void createMissingUserDocument(FirebaseUser firebaseUser) {
        profileViewModel.createMissingUserDocument(firebaseUser).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                currentUser = user;
                populateUserData(user);
                Toast.makeText(getContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(User user) {
        binding.tvFullName.setText(user.getFullName());
        binding.tvEmail.setText(user.getEmail());
        binding.tvRole.setText(user.getRole());
        binding.tvLevel.setText(user.getLevel());
        binding.tvGroups.setText(user.getGroups() != null && !user.getGroups().isEmpty()
                ? user.getGroups()
                : "-");

        // Calculate and set groups count - handle null/empty cases
        int groupsCount = 0;
        String groups = user.getGroups();
        if (groups != null && !groups.isEmpty() && !groups.equals("-") && !groups.trim().isEmpty()) {
            // Count comma-separated groups, filtering out empty strings
            String[] groupsArray = groups.split(",");
            for (String group : groupsArray) {
                if (group != null && !group.trim().isEmpty()) {
                    groupsCount++;
                }
            }
        }
        binding.tvGroupsCount.setText(String.valueOf(groupsCount));

        // Bio
        if (user.getBio() != null && !user.getBio().isEmpty()) {
            binding.tvBio.setText(user.getBio());
        } else {
            binding.tvBio.setText(user.getRole() + " • " + user.getLevel());
        }

        // Mini-CV
        if (user.getSkills() != null && !user.getSkills().isEmpty()) {
            binding.tvSkills.setText(android.text.TextUtils.join(", ", user.getSkills()));
            ((View) binding.tvSkills.getParent().getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) binding.tvSkills.getParent().getParent()).setVisibility(View.GONE);
        }

        if (user.getLinkedin() != null && !user.getLinkedin().isEmpty()) {
            binding.tvLinkedin.setText(user.getLinkedin());
            ((View) binding.tvLinkedin.getParent().getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) binding.tvLinkedin.getParent().getParent()).setVisibility(View.GONE);
        }

        if (user.getPortfolio() != null && !user.getPortfolio().isEmpty()) {
            binding.tvPortfolio.setText(user.getPortfolio());
            ((View) binding.tvPortfolio.getParent().getParent()).setVisibility(View.VISIBLE);
        } else {
            ((View) binding.tvPortfolio.getParent().getParent()).setVisibility(View.GONE);
        }

        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            Glide.with(this)
                    .load(user.getProfileImage())
                    .placeholder(R.drawable.logo)
                    .into(binding.ivProfileImage);
        }
    }

    private void loadUserMedia(String userId) {
        mediaViewModel.getUserMedia(userId).observe(getViewLifecycleOwner(), mediaList -> {
            if (mediaList != null) {
                if (!mediaList.isEmpty()) {
                    mediaItems.clear();
                    mediaItems.addAll(mediaList);
                    mediaAdapter.updateMedia(mediaItems);
                    binding.tvNoMedia.setVisibility(View.GONE);
                    binding.recyclerViewMedia.setVisibility(View.VISIBLE);
                }
                binding.tvMediaCount.setText(String.valueOf(mediaList.size()));
            } else {
                binding.tvNoMedia.setVisibility(View.VISIBLE);
                binding.recyclerViewMedia.setVisibility(View.GONE);
                binding.tvMediaCount.setText("0");
            }
        });
    }

    private void loadUserPosts(String userId) {
        feedViewModel.getUserPosts(userId).observe(getViewLifecycleOwner(), postList -> {
            if (postList != null) {
                if (!postList.isEmpty()) {
                    posts.clear();
                    posts.addAll(postList);
                    postAdapter.updatePosts(posts);
                    binding.tvNoPosts.setVisibility(View.GONE);
                    binding.recyclerViewPosts.setVisibility(View.VISIBLE);
                }
                binding.tvPostsCount.setText(String.valueOf(postList.size()));
            } else {
                binding.tvNoPosts.setVisibility(View.VISIBLE);
                binding.recyclerViewPosts.setVisibility(View.GONE);
                binding.tvPostsCount.setText("0");
            }
        });
    }

    private void uploadProfileImage(android.net.Uri uri) {
        binding.progressBar.setVisibility(View.VISIBLE);

        mediaViewModel.updateProfileImage(uri).observe(getViewLifecycleOwner(), success -> {
            binding.progressBar.setVisibility(View.GONE);

            if (success) {
                Toast.makeText(requireContext(), R.string.profile_updated, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openMediaViewer(MediaItem mediaItem) {
        Bundle args = new Bundle();
        args.putString("mediaUrl", mediaItem.getUrl());
        args.putString("mediaType", mediaItem.getType());
        NavHostFragment.findNavController(this).navigate(R.id.mediaViewerFragment, args);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
