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

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.FeedPostAdapter;
import i.imessenger.databinding.FragmentUserPostsBinding;
import i.imessenger.models.FeedPost;
import i.imessenger.viewmodels.FeedViewModel;

public class UserPostsFragment extends Fragment implements FeedPostAdapter.OnPostInteractionListener {

    private FragmentUserPostsBinding binding;
    private FeedViewModel feedViewModel;
    private FeedPostAdapter adapter;
    private List<FeedPost> posts = new ArrayList<>();
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentUserPostsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        if (userId == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        setupToolbar();
        setupRecyclerView();
        loadPosts();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new FeedPostAdapter(requireContext(), posts,
                feedViewModel.getCurrentUserId(), this);
        binding.recyclerViewPosts.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewPosts.setAdapter(adapter);
    }

    private void loadPosts() {
        binding.progressBar.setVisibility(View.VISIBLE);

        feedViewModel.getUserPosts(userId).observe(getViewLifecycleOwner(), postList -> {
            binding.progressBar.setVisibility(View.GONE);

            if (postList != null && !postList.isEmpty()) {
                posts.clear();
                posts.addAll(postList);
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
        // Already on user's posts page
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
        // TODO: Handle report click
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
