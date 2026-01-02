package i.imessenger.adapters;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import i.imessenger.R;

public class SelectedMediaAdapter extends RecyclerView.Adapter<SelectedMediaAdapter.MediaViewHolder> {

    private List<Uri> mediaUris;
    private List<String> mediaTypes;
    private OnRemoveMediaListener listener;

    public interface OnRemoveMediaListener {
        void onRemove(int position);
    }

    // New constructor with mediaTypes
    public SelectedMediaAdapter(List<Uri> mediaUris, List<String> mediaTypes, OnRemoveMediaListener listener) {
        this.mediaUris = mediaUris;
        this.mediaTypes = mediaTypes;
        this.listener = listener;
    }

    // Legacy constructor for backwards compatibility
    public SelectedMediaAdapter(Context context, List<Uri> mediaUris, OnRemoveMediaListener listener) {
        this.mediaUris = mediaUris;
        this.mediaTypes = null;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Uri uri = mediaUris.get(position);
        Context context = holder.itemView.getContext();

        Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.logo)
                .centerCrop()
                .into(holder.ivMedia);

        // Show video indicator if applicable
        if (holder.ivVideoIndicator != null) {
            if (mediaTypes != null && position < mediaTypes.size() && "video".equals(mediaTypes.get(position))) {
                holder.ivVideoIndicator.setVisibility(View.VISIBLE);
            } else {
                holder.ivVideoIndicator.setVisibility(View.GONE);
            }
        }

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaUris != null ? mediaUris.size() : 0;
    }

    public void updateMedia(List<Uri> newMedia) {
        this.mediaUris = newMedia;
        notifyDataSetChanged();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedia;
        ImageView ivVideoIndicator;
        ImageButton btnRemove;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            ivVideoIndicator = itemView.findViewById(R.id.ivVideoIndicator);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

