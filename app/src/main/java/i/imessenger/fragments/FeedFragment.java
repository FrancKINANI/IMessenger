package i.imessenger.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.FeedPostAdapter;
import i.imessenger.databinding.FragmentFeedBinding;
import i.imessenger.models.FeedPost;
import i.imessenger.viewmodels.FeedViewModel;

public class FeedFragment extends Fragment implements FeedPostAdapter.OnPostInteractionListener {

    private FragmentFeedBinding binding;
    private FeedViewModel feedViewModel;
    private FeedPostAdapter adapter;
    private List<FeedPost> posts = new ArrayList<>();

    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentFeedBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        setupRecyclerView();
        setupSwipeRefresh();
        setupClickListeners();
        observeData();
    }

    private void setupRecyclerView() {
        adapter = new FeedPostAdapter(requireContext(), posts,
                feedViewModel.getCurrentUserId(), this);
        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPosts.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setColorSchemeResources(R.color.ismagi_accent);
        binding.swipeRefresh.setOnRefreshListener(() -> {
            // Data is already being observed in real-time, just stop the animation
            binding.swipeRefresh.setRefreshing(false);
        });
    }

    private void setupClickListeners() {
        binding.fabCreatePost.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.createPostFragment);
        });

        binding.ivUserAvatar.setOnClickListener(v -> {
            NavHostFragment.findNavController(this).navigate(R.id.nav_profile);
        });
    }

    private void observeData() {
        // Load current user avatar
        feedViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.logo)
                        .circleCrop()
                        .into(binding.ivUserAvatar);
            }
        });

        // Load feed posts
        feedViewModel.getFeedPosts().observe(getViewLifecycleOwner(), feedPosts -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.swipeRefresh.setRefreshing(false);

            if (feedPosts != null && !feedPosts.isEmpty()) {
                posts.clear();
                posts.addAll(feedPosts);
                adapter.updatePosts(posts);
                binding.emptyState.setVisibility(View.GONE);
                binding.recyclerViewPosts.setVisibility(View.VISIBLE);
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewPosts.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onLikeClicked(FeedPost post) {
        feedViewModel.toggleLike(post.getPostId());
    }

    @Override
    public void onCommentClicked(FeedPost post) {
        Bundle args = new Bundle();
        args.putString("postId", post.getPostId());
        NavHostFragment.findNavController(this).navigate(R.id.commentsFragment, args);
    }

    @Override
    public void onShareClicked(FeedPost post) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, post.getContent());
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }

    @Override
    public void onAuthorClicked(FeedPost post) {
        if (feedViewModel.isCurrentUser(post.getAuthorId())) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_profile);
        } else {
            Bundle args = new Bundle();
            args.putString("userId", post.getAuthorId());
            NavHostFragment.findNavController(this).navigate(R.id.userProfileFragment, args);
        }
    }

    @Override
    public void onMediaClicked(FeedPost post, int mediaIndex) {
        Bundle args = new Bundle();
        args.putString("mediaUrl", post.getMediaUrls().get(mediaIndex));
        args.putString("mediaType", post.getMediaTypes().isEmpty() ? "image" : post.getMediaTypes().get(mediaIndex));
        NavHostFragment.findNavController(this).navigate(R.id.mediaViewerFragment, args);
    }

    @Override
    public void onDeleteClicked(FeedPost post) {
        feedViewModel.deletePost(post.getPostId()).observe(getViewLifecycleOwner(), success -> {
            if (success) {
                Toast.makeText(requireContext(), R.string.post_deleted, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReportClicked(FeedPost post) {
        String[] reasons = { "Spam", "Inappropriate Content", "Harassment", "Other" };
        new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setTitle("Report Post")
                .setItems(reasons, (dialog, which) -> {
                    String reason = reasons[which];
                    feedViewModel.reportPost(post.getPostId(), reason).observe(getViewLifecycleOwner(), success -> {
                        if (success) {
                            Toast.makeText(requireContext(), "Report sent. Thank you.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(requireContext(), "Failed to report post.", Toast.LENGTH_SHORT).show();
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
