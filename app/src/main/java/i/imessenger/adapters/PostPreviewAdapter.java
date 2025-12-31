package i.imessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import i.imessenger.R;
import i.imessenger.databinding.ItemPostPreviewBinding;
import i.imessenger.models.FeedPost;

public class PostPreviewAdapter extends RecyclerView.Adapter<PostPreviewAdapter.PreviewViewHolder> {

    private Context context;
    private List<FeedPost> posts;
    private OnPostClickListener listener;

    public interface OnPostClickListener {
        void onPostClicked(FeedPost post);
    }

    public PostPreviewAdapter(Context context, List<FeedPost> posts, OnPostClickListener listener) {
        this.context = context;
        this.posts = posts;
        this.listener = listener;
    }

    @NonNull
    @Override
    public PreviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPostPreviewBinding binding = ItemPostPreviewBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new PreviewViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PreviewViewHolder holder, int position) {
        FeedPost post = posts.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return Math.min(posts.size(), 3); // Show only 3 preview posts
    }

    public void updatePosts(List<FeedPost> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }

    class PreviewViewHolder extends RecyclerView.ViewHolder {
        ItemPostPreviewBinding binding;

        PreviewViewHolder(ItemPostPreviewBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(FeedPost post) {
            // Author image
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
            binding.tvContent.setText(post.getContent() != null ? post.getContent() : "");

            // Timestamp and stats
            String timeAgo = getTimeAgo(post.getCreatedAt());
            int likes = post.getLikeCount();
            binding.tvTimestamp.setText(timeAgo + " • " + likes + " " + context.getString(R.string.likes));

            // Post image (if has media)
            if (post.hasMedia() && !post.getMediaUrls().isEmpty()) {
                binding.postImageContainer.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(post.getMediaUrls().get(0))
                        .placeholder(R.drawable.logo)
                        .centerCrop()
                        .into(binding.ivPostImage);
            } else {
                binding.postImageContainer.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onPostClicked(post);
            });
        }

        private String getTimeAgo(Timestamp timestamp) {
            if (timestamp == null) return "";

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

