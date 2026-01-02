package i.imessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import i.imessenger.R;
import i.imessenger.models.MediaItem;

public class MediaGridAdapter extends RecyclerView.Adapter<MediaGridAdapter.MediaViewHolder> {

    private Context context;
    private List<MediaItem> mediaItems;
    private OnMediaClickListener listener;

    // For URL-based media
    private List<String> mediaUrls;
    private List<String> mediaTypes;
    private OnUrlMediaClickListener urlListener;

    public interface OnMediaClickListener {
        void onMediaClicked(MediaItem mediaItem, int position);
        void onMediaLongClicked(MediaItem mediaItem, int position);
    }

    public interface OnUrlMediaClickListener {
        void onMediaClicked(String url, int position);
    }

    public MediaGridAdapter(Context context, List<MediaItem> mediaItems, OnMediaClickListener listener) {
        this.context = context;
        this.mediaItems = mediaItems;
        this.listener = listener;
        this.mediaUrls = null;
        this.mediaTypes = null;
    }

    // Constructor for URL-based media (used in feed posts)
    public MediaGridAdapter(Context context, List<String> mediaUrls, List<String> mediaTypes, OnUrlMediaClickListener listener) {
        this.context = context;
        this.mediaUrls = mediaUrls;
        this.mediaTypes = mediaTypes;
        this.urlListener = listener;
        this.mediaItems = null;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media_grid, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        if (mediaUrls != null) {
            // URL-based binding
            String url = mediaUrls.get(position);
            String type = (mediaTypes != null && position < mediaTypes.size()) ? mediaTypes.get(position) : "image";
            holder.bindUrl(url, type, position);
        } else if (mediaItems != null) {
            // MediaItem-based binding
            MediaItem item = mediaItems.get(position);
            holder.bind(item, position);
        }
    }

    @Override
    public int getItemCount() {
        if (mediaUrls != null) {
            return mediaUrls.size();
        }
        return mediaItems != null ? mediaItems.size() : 0;
    }

    public void updateMedia(List<MediaItem> newMedia) {
        this.mediaItems = newMedia;
        notifyDataSetChanged();
    }

    class MediaViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView ivThumbnail;
        View videoOverlay;
        ImageView ivPlayIcon;
        TextView tvDuration;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardView);
            ivThumbnail = itemView.findViewById(R.id.ivThumbnail);
            videoOverlay = itemView.findViewById(R.id.videoOverlay);
            ivPlayIcon = itemView.findViewById(R.id.ivPlayIcon);
            tvDuration = itemView.findViewById(R.id.tvDuration);
        }

        void bind(MediaItem item, int position) {
            String thumbnailUrl = item.getThumbnailUrl() != null ? item.getThumbnailUrl() : item.getUrl();

            Glide.with(context)
                    .load(thumbnailUrl)
                    .placeholder(R.drawable.logo)
                    .centerCrop()
                    .into(ivThumbnail);

            if (item.isVideo()) {
                videoOverlay.setVisibility(View.VISIBLE);
                ivPlayIcon.setVisibility(View.VISIBLE);
                if (item.getDuration() > 0) {
                    tvDuration.setText(item.getFormattedDuration());
                    tvDuration.setVisibility(View.VISIBLE);
                } else {
                    tvDuration.setVisibility(View.GONE);
                }
            } else {
                videoOverlay.setVisibility(View.GONE);
                ivPlayIcon.setVisibility(View.GONE);
                tvDuration.setVisibility(View.GONE);
            }

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

        void bindUrl(String url, String type, int position) {
            if ("document".equals(type)) {
                ivThumbnail.setImageResource(R.drawable.ic_document);
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER);
                videoOverlay.setVisibility(View.GONE);
                ivPlayIcon.setVisibility(View.GONE);
                tvDuration.setVisibility(View.GONE);
            } else if ("video".equals(type)) {
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.logo)
                        .centerCrop()
                        .into(ivThumbnail);
                videoOverlay.setVisibility(View.VISIBLE);
                ivPlayIcon.setVisibility(View.VISIBLE);
                tvDuration.setVisibility(View.GONE);
            } else {
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                Glide.with(context)
                        .load(url)
                        .placeholder(R.drawable.logo)
                        .centerCrop()
                        .into(ivThumbnail);
                videoOverlay.setVisibility(View.GONE);
                ivPlayIcon.setVisibility(View.GONE);
                tvDuration.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (urlListener != null) urlListener.onMediaClicked(url, position);
            });
        }
    }
}

