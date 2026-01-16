package i.imessenger.adapters;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import i.imessenger.R;
import i.imessenger.databinding.ItemDocumentBinding;
import i.imessenger.models.DocumentFile;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {

    private List<DocumentFile> documents = new ArrayList<>();
    private final OnDocumentClickListener listener;
    private Context context;

    public interface OnDocumentClickListener {
        void onDownloadClick(DocumentFile file);

        void onDeleteClick(DocumentFile file);

        void onItemClick(DocumentFile file);
    }

    public DocumentAdapter(OnDocumentClickListener listener) {
        this.listener = listener;
    }

    public void setDocuments(List<DocumentFile> documents) {
        this.documents = documents;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        ItemDocumentBinding binding = ItemDocumentBinding.inflate(
                LayoutInflater.from(context), parent, false);
        return new DocumentViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        holder.bind(documents.get(position));
    }

    @Override
    public int getItemCount() {
        return documents.size();
    }

    class DocumentViewHolder extends RecyclerView.ViewHolder {
        private final ItemDocumentBinding binding;

        public DocumentViewHolder(ItemDocumentBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(DocumentFile file) {
            binding.tvName.setText(file.getName());

            String size = Formatter.formatFileSize(context, file.getSizeBytes());
            String timeAgo = getTimeAgo(file.getCreatedAt());

            binding.tvInfo.setText(size + " • " + timeAgo);
            binding.tvUploader.setText("by " + file.getUploaderName());

            // Set icon based on type (simple logic for now)
            int iconRes = R.drawable.ic_document; // Default
            if (file.getType() != null) {
                if (file.getType().contains("pdf"))
                    iconRes = R.drawable.ic_document; // Use specific icon if available
                // Add more types...
            }
            binding.ivIcon.setImageResource(iconRes);

            binding.getRoot().setOnClickListener(v -> listener.onItemClick(file));
            binding.btnDownload.setOnClickListener(v -> listener.onDownloadClick(file));

            // Handle More button for delete option
            binding.btnMore.setOnClickListener(v -> {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(context, v);
                popup.getMenu().add("Delete"); // Only show if user is owner/admin? logic can be added here
                popup.setOnMenuItemClickListener(item -> {
                    if (item.getTitle().equals("Delete")) {
                        listener.onDeleteClick(file);
                        return true;
                    }
                    return false;
                });
                popup.show();
            });
        }

        private String getTimeAgo(com.google.firebase.Timestamp timestamp) {
            if (timestamp == null)
                return "";
            long now = System.currentTimeMillis();
            long time = timestamp.toDate().getTime();
            long diff = now - time;

            long days = TimeUnit.MILLISECONDS.toDays(diff);
            if (days == 0)
                return "Today";
            return days + "d ago";
        }
    }
}
