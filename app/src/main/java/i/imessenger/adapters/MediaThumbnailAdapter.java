package i.imessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import i.imessenger.R;
import i.imessenger.databinding.ItemMediaThumbnailBinding;
import i.imessenger.models.MediaItem;

public class MediaThumbnailAdapter extends RecyclerView.Adapter<MediaThumbnailAdapter.MediaViewHolder> {

    private Context context;
    private List<MediaItem> mediaItems;
    private OnMediaClickListener listener;

    public interface OnMediaClickListener {
        void onMediaClicked(MediaItem mediaItem, int position);
        void onMediaLongClicked(MediaItem mediaItem, int position);
    }

    public MediaThumbnailAdapter(Context context, List<MediaItem> mediaItems, OnMediaClickListener listener) {
        this.context = context;
        this.mediaItems = mediaItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemMediaThumbnailBinding binding = ItemMediaThumbnailBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new MediaViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem item = mediaItems.get(position);
        holder.bind(item, position);
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    public void updateMedia(List<MediaItem> newMedia) {
        this.mediaItems = newMedia;
        notifyDataSetChanged();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        ItemMediaThumbnailBinding binding;

        MediaViewHolder(ItemMediaThumbnailBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(MediaItem item, int position) {
            // Load thumbnail
            String thumbnailUrl = item.getThumbnailUrl() != null ? item.getThumbnailUrl() : item.getUrl();
            Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.logo)
                    .centerCrop()
                    .into(binding.ivThumbnail);

            // Show video indicator if it's a video
            if (item.isVideo()) {
                binding.videoIndicator.setVisibility(View.VISIBLE);
                if (item.getDuration() > 0) {
                    binding.tvDuration.setText(item.getFormattedDuration());
                    binding.tvDuration.setVisibility(View.VISIBLE);
                } else {
                    binding.tvDuration.setVisibility(View.GONE);
                }
            } else {
                binding.videoIndicator.setVisibility(View.GONE);
            }

            // Click listeners
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMediaClicked(item, position);
            });

            itemView.setOnLongClickListener(v -> {
                if (listener != null) {
                    listener.onMediaLongClicked(item, position);
                    return true;
                }
                return false;
            });
        }
    }
}

