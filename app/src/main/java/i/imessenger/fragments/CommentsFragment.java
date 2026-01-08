package i.imessenger.fragments;

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
import i.imessenger.adapters.CommentAdapter;
import i.imessenger.databinding.FragmentCommentsBinding;
import i.imessenger.models.Comment;
import i.imessenger.viewmodels.FeedViewModel;

public class CommentsFragment extends Fragment implements CommentAdapter.OnCommentClickListener {

    private FragmentCommentsBinding binding;
    private FeedViewModel feedViewModel;
    private CommentAdapter adapter;
    private List<Comment> comments = new ArrayList<>();
    private String postId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentCommentsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            postId = getArguments().getString("postId");
        }

        if (postId == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        feedViewModel = new ViewModelProvider(this).get(FeedViewModel.class);

        // Increment view count
        feedViewModel.incrementViewCount(postId);

        setupToolbar();
        setupRecyclerView();
        setupClickListeners();
        loadUserAvatar();
        loadComments();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
    }

    private void setupRecyclerView() {
        adapter = new CommentAdapter(requireContext(), comments, this);
        binding.recyclerViewComments.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerViewComments.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.btnSendComment.setOnClickListener(v -> sendComment());
    }

    private void loadUserAvatar() {
        feedViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null && user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.logo)
                        .circleCrop()
                        .into(binding.ivUserAvatar);
            }
        });
    }

    private void loadComments() {
        binding.progressBar.setVisibility(View.VISIBLE);

        feedViewModel.getComments(postId).observe(getViewLifecycleOwner(), commentList -> {
            binding.progressBar.setVisibility(View.GONE);

            if (commentList != null && !commentList.isEmpty()) {
                comments.clear();
                comments.addAll(commentList);
                adapter.updateComments(comments);
                binding.emptyState.setVisibility(View.GONE);
                binding.recyclerViewComments.setVisibility(View.VISIBLE);
            } else {
                binding.emptyState.setVisibility(View.VISIBLE);
                binding.recyclerViewComments.setVisibility(View.GONE);
            }
        });
    }

    private void sendComment() {
        String content = binding.etComment.getText().toString().trim();

        if (content.isEmpty()) {
            return;
        }

        binding.btnSendComment.setEnabled(false);

        feedViewModel.addComment(postId, content).observe(getViewLifecycleOwner(), success -> {
            binding.btnSendComment.setEnabled(true);

            if (success) {
                binding.etComment.setText("");
                // Scroll to bottom
                if (!comments.isEmpty()) {
                    binding.recyclerViewComments.scrollToPosition(comments.size() - 1);
                }
            } else {
                Toast.makeText(requireContext(), R.string.error_occurred, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onAuthorClicked(Comment comment) {
        if (feedViewModel.isCurrentUser(comment.getAuthorId())) {
            NavHostFragment.findNavController(this).navigate(R.id.nav_profile);
        } else {
            Bundle args = new Bundle();
            args.putString("userId", comment.getAuthorId());
            NavHostFragment.findNavController(this).navigate(R.id.userProfileFragment, args);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
