package i.imessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import i.imessenger.R;
import i.imessenger.databinding.ItemFeedPostBinding;
import i.imessenger.models.FeedPost;

public class FeedPostAdapter extends RecyclerView.Adapter<FeedPostAdapter.PostViewHolder> {

    private Context context;
    private List<FeedPost> posts;
    private String currentUserId;
    private OnPostInteractionListener listener;

    public interface OnPostInteractionListener {
        void onLikeClicked(FeedPost post);

        void onCommentClicked(FeedPost post);

        void onShareClicked(FeedPost post);

        void onAuthorClicked(FeedPost post);

        void onMediaClicked(FeedPost post, int mediaIndex);

        void onDeleteClicked(FeedPost post);

        void onReportClicked(FeedPost post);
    }

    public FeedPostAdapter(Context context, List<FeedPost> posts, String currentUserId,
            OnPostInteractionListener listener) {
        this.context = context;
        this.posts = posts;
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemFeedPostBinding binding = ItemFeedPostBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new PostViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        FeedPost post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public void updatePosts(List<FeedPost> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    class PostViewHolder extends RecyclerView.ViewHolder {
        ItemFeedPostBinding binding;

        PostViewHolder(ItemFeedPostBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FeedPost post) {
            // Author info
            binding.tvAuthorName.setText(post.getAuthorName());
            binding.tvAuthorRole.setText(formatAuthorRole(post));

            if (post.getAuthorImage() != null && !post.getAuthorImage().isEmpty()) {
                Glide.with(context)
                        .load(post.getAuthorImage())
                        .placeholder(R.drawable.logo)
                        .circleCrop()
                        .into(binding.ivAuthorImage);
            } else {
                binding.ivAuthorImage.setImageResource(R.drawable.logo);
            }

            // Content
            if (post.getContent() != null && !post.getContent().isEmpty()) {
                binding.tvContent.setVisibility(View.VISIBLE);
                binding.tvContent.setText(post.getContent());
            } else {
                binding.tvContent.setVisibility(View.GONE);
            }

            // Media
            if (post.hasMedia()) {
                binding.mediaContainer.setVisibility(View.VISIBLE);
                setupMedia(post);
            } else {
                binding.mediaContainer.setVisibility(View.GONE);
            }

            // Stats & Actions Integration
            int likeCount = post.getLikeCount();
            int commentCount = post.getCommentCount();

            // Hide legacy stats layout (we show counts in buttons now)
            binding.statsLayout.setVisibility(View.GONE);

            // Update Action Buttons with Counts
            if (likeCount > 0) {
                binding.tvLike.setText(String.valueOf(likeCount));
            } else {
                binding.tvLike.setText(context.getString(R.string.like));
            }

            if (commentCount > 0) {
                binding.tvComment.setText(String.valueOf(commentCount));
            } else {
                binding.tvComment.setText(context.getString(R.string.comment));
            }

            // View Count
            int viewCount = post.getViewCount();
            if (viewCount > 0) {
                binding.llStats.setVisibility(View.VISIBLE);
                binding.tvViewCount.setText(viewCount + " views");
            } else {
                binding.llStats.setVisibility(View.GONE);
            }

            // Like state
            boolean isLiked = post.isLikedBy(currentUserId);
            updateLikeButton(isLiked);

            // Click listeners
            binding.ivAuthorImage.setOnClickListener(v -> {
                if (listener != null)
                    listener.onAuthorClicked(post);
            });

            binding.tvAuthorName.setOnClickListener(v -> {
                if (listener != null)
                    listener.onAuthorClicked(post);
            });

            binding.btnLike.setOnClickListener(v -> {
                if (listener != null)
                    listener.onLikeClicked(post);
            });

            binding.btnComment.setOnClickListener(v -> {
                if (listener != null)
                    listener.onCommentClicked(post);
            });

            binding.btnShare.setOnClickListener(v -> {
                if (listener != null)
                    listener.onShareClicked(post);
            });

            binding.btnMore.setOnClickListener(v -> showPostMenu(v, post));

            binding.ivSingleMedia.setOnClickListener(v -> {
                if (listener != null)
                    listener.onMediaClicked(post, 0);
            });

            binding.videoContainer.setOnClickListener(v -> {
                if (listener != null)
                    listener.onMediaClicked(post, 0);
            });
        }

        private void setupMedia(FeedPost post) {
            List<String> mediaUrls = post.getMediaUrls();
            List<String> mediaTypes = post.getMediaTypes();
            List<String> mediaNames = post.getMediaNames();

            if (mediaUrls.size() == 1) {
                String type = mediaTypes.isEmpty() ? "image" : mediaTypes.get(0);
                String name = (mediaNames != null && !mediaNames.isEmpty()) ? mediaNames.get(0) : "file";

                if ("video".equals(type)) {
                    binding.ivSingleMedia.setVisibility(View.GONE);
                    binding.videoContainer.setVisibility(View.VISIBLE);
                    binding.recyclerViewMedia.setVisibility(View.GONE);

                    Glide.with(context)
                            .load(mediaUrls.get(0))
                            .placeholder(R.drawable.logo)
                            .into(binding.ivVideoThumbnail);
                } else if ("document".equals(type)) {
                    binding.ivSingleMedia.setVisibility(View.VISIBLE);
                    binding.videoContainer.setVisibility(View.GONE);
                    binding.recyclerViewMedia.setVisibility(View.GONE);

                    // Show document icon
                    binding.ivSingleMedia.setImageResource(R.drawable.ic_document);
                    binding.ivSingleMedia.setScaleType(android.widget.ImageView.ScaleType.CENTER);
                } else {
                    binding.ivSingleMedia.setVisibility(View.VISIBLE);
                    binding.videoContainer.setVisibility(View.GONE);
                    binding.recyclerViewMedia.setVisibility(View.GONE);

                    binding.ivSingleMedia.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                    Glide.with(context)
                            .load(mediaUrls.get(0))
                            .placeholder(R.drawable.logo)
                            .into(binding.ivSingleMedia);
                }
            } else {
                // Multiple media - use recycler view grid
                binding.ivSingleMedia.setVisibility(View.GONE);
                binding.videoContainer.setVisibility(View.GONE);
                binding.recyclerViewMedia.setVisibility(View.VISIBLE);

                MediaGridAdapter adapter = new MediaGridAdapter(context, mediaUrls, mediaTypes, (url, position) -> {
                    if (listener != null)
                        listener.onMediaClicked(post, position);
                });
                binding.recyclerViewMedia.setAdapter(adapter);
            }
        }

        private void updateLikeButton(boolean isLiked) {
            if (isLiked) {
                binding.ivLike.setImageResource(R.drawable.ic_like_filled);
                binding.ivLike.setColorFilter(context.getColor(R.color.colorLogout));
                binding.tvLike.setTextColor(context.getColor(R.color.colorLogout));
            } else {
                binding.ivLike.setImageResource(R.drawable.ic_like_outline);
                binding.ivLike.setColorFilter(context.getColor(R.color.text_secondary));
                binding.tvLike.setTextColor(context.getColor(R.color.text_secondary));
            }
        }

        private void showPostMenu(View anchor, FeedPost post) {
            PopupMenu popup = new PopupMenu(context, anchor);

            if (currentUserId != null && currentUserId.equals(post.getAuthorId())) {
                popup.getMenu().add(0, 1, 0, R.string.delete);
            }
            popup.getMenu().add(0, 2, 0, R.string.share);
            popup.getMenu().add(0, 3, 0, "Report");

            popup.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == 1 && listener != null) {
                    listener.onDeleteClicked(post);
                    return true;
                } else if (item.getItemId() == 2 && listener != null) {
                    listener.onShareClicked(post);
                    return true;
                } else if (item.getItemId() == 3 && listener != null) {
                    listener.onReportClicked(post);
                    return true;
                }
                return false;
            });

            popup.show();
        }

        private String formatAuthorRole(FeedPost post) {
            String role = post.getAuthorRole() != null ? post.getAuthorRole() : "";
            String timeAgo = getTimeAgo(post.getCreatedAt());

            if (!role.isEmpty()) {
                return role + " • " + timeAgo;
            }
            return timeAgo;
        }

        private String getTimeAgo(Timestamp timestamp) {
            if (timestamp == null)
                return "";

            long now = System.currentTimeMillis();
            long time = timestamp.toDate().getTime();
            long diff = now - time;

            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            long days = TimeUnit.MILLISECONDS.toDays(diff);

            if (minutes < 1) {
                return context.getString(R.string.just_now);
            } else if (minutes < 60) {
                return context.getString(R.string.minutes_ago, (int) minutes);
            } else if (hours < 24) {
                return context.getString(R.string.hours_ago, (int) hours);
            } else {
                return context.getString(R.string.days_ago, (int) days);
            }
        }
    }
}
