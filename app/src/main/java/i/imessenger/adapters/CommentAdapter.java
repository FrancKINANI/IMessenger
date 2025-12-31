package i.imessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;

import java.util.List;
import java.util.concurrent.TimeUnit;

import i.imessenger.R;
import i.imessenger.databinding.ItemCommentBinding;
import i.imessenger.models.Comment;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private Context context;
    private List<Comment> comments;
    private OnCommentClickListener listener;

    public interface OnCommentClickListener {
        void onAuthorClicked(Comment comment);
    }

    public CommentAdapter(Context context, List<Comment> comments, OnCommentClickListener listener) {
        this.context = context;
        this.comments = comments;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCommentBinding binding = ItemCommentBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new CommentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.bind(comment);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void updateComments(List<Comment> newComments) {
        this.comments = newComments;
        notifyDataSetChanged();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder {
        ItemCommentBinding binding;

        CommentViewHolder(ItemCommentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Comment comment) {
            binding.tvAuthorName.setText(comment.getAuthorName());
            binding.tvContent.setText(comment.getContent());
            binding.tvTimestamp.setText(getTimeAgo(comment.getCreatedAt()));

            if (comment.getAuthorImage() != null && !comment.getAuthorImage().isEmpty()) {
                Glide.with(context)
                        .load(comment.getAuthorImage())
                        .placeholder(R.drawable.logo)
                        .circleCrop()
                        .into(binding.ivAuthorImage);
            } else {
                binding.ivAuthorImage.setImageResource(R.drawable.logo);
            }

            binding.ivAuthorImage.setOnClickListener(v -> {
                if (listener != null) listener.onAuthorClicked(comment);
            });

            binding.tvAuthorName.setOnClickListener(v -> {
                if (listener != null) listener.onAuthorClicked(comment);
            });
        }

        private String getTimeAgo(Timestamp timestamp) {
            if (timestamp == null) return "";

            long now = System.currentTimeMillis();
            long time = timestamp.toDate().getTime();
            long diff = now - time;

            long minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(diff);
            long hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(diff);
            long days = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diff);

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

