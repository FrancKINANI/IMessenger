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

    private Context context;
    private List<Uri> mediaUris;
    private OnRemoveMediaListener listener;

    public interface OnRemoveMediaListener {
        void onRemove(int position);
    }

    public SelectedMediaAdapter(Context context, List<Uri> mediaUris, OnRemoveMediaListener listener) {
        this.context = context;
        this.mediaUris = mediaUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_selected_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        Uri uri = mediaUris.get(position);

        Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.logo)
                .centerCrop()
                .into(holder.ivMedia);

        holder.btnRemove.setOnClickListener(v -> {
            if (listener != null) {
                listener.onRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaUris.size();
    }

    public void updateMedia(List<Uri> newMedia) {
        this.mediaUris = newMedia;
        notifyDataSetChanged();
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        ImageView ivMedia;
        ImageButton btnRemove;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            ivMedia = itemView.findViewById(R.id.ivMedia);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

