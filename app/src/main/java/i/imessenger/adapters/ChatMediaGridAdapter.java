package i.imessenger.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

import i.imessenger.R;

public class ChatMediaGridAdapter extends RecyclerView.Adapter<ChatMediaGridAdapter.MediaViewHolder> {

    private final List<String> mediaUrls;
    private final List<String> mediaTypes;
    private final List<String> mediaNames;

    public ChatMediaGridAdapter(List<String> mediaUrls, List<String> mediaTypes, List<String> mediaNames) {
        this.mediaUrls = mediaUrls;
        this.mediaTypes = mediaTypes;
        this.mediaNames = mediaNames;
    }

    // Legacy constructor for backwards compatibility
    public ChatMediaGridAdapter(List<String> mediaUrls, List<String> mediaTypes) {
        this.mediaUrls = mediaUrls;
        this.mediaTypes = mediaTypes;
        this.mediaNames = null;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_media_grid, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        String url = mediaUrls.get(position);
        String type = mediaTypes != null && position < mediaTypes.size() ? mediaTypes.get(position) : "image";
        String name = mediaNames != null && position < mediaNames.size() ? mediaNames.get(position) : "file";
        Context context = holder.itemView.getContext();

        if ("document".equals(type)) {
            holder.imageView.setImageResource(R.drawable.ic_document);
            holder.imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER);
            holder.playIcon.setVisibility(View.GONE);
        } else {
            holder.imageView.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
            Glide.with(context)
                    .load(url)
                    .transform(new CenterCrop(), new RoundedCorners(12))
                    .into(holder.imageView);

            if ("video".equals(type)) {
                holder.playIcon.setVisibility(View.VISIBLE);
            } else {
                holder.playIcon.setVisibility(View.GONE);
            }
        }

        holder.container.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if ("video".equals(type)) {
                    intent.setDataAndType(Uri.parse(url), "video/*");
                } else if ("document".equals(type)) {
                    intent.setDataAndType(Uri.parse(url), "*/*");
                } else {
                    intent.setDataAndType(Uri.parse(url), "image/*");
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mediaUrls != null ? mediaUrls.size() : 0;
    }

    static class MediaViewHolder extends RecyclerView.ViewHolder {
        FrameLayout container;
        ImageView imageView;
        ImageView playIcon;

        MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            container = itemView.findViewById(R.id.mediaContainer);
            imageView = itemView.findViewById(R.id.mediaImage);
            playIcon = itemView.findViewById(R.id.playIcon);
        }
    }
}

