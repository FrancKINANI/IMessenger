package I.imessenger.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import I.imessenger.R;
import I.imessenger.activities.ChatActivity;
import I.imessenger.databinding.ItemHeaderBinding;
import I.imessenger.databinding.ItemConversationModernBinding;
import I.imessenger.models.ChatConversation;

public class ConversationsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private List<ChatConversation> conversationList;

    public ConversationsAdapter(Context context, List<ChatConversation> conversationList) {
        this.context = context;
        this.conversationList = conversationList;
    }

    @Override
    public int getItemViewType(int position) {
        return conversationList.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatConversation.TYPE_HEADER) {
            ItemHeaderBinding binding = ItemHeaderBinding.inflate(LayoutInflater.from(context), parent, false);
            return new HeaderViewHolder(binding);
        } else {
            ItemConversationModernBinding binding = ItemConversationModernBinding.inflate(LayoutInflater.from(context), parent, false);
            return new ConversationViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatConversation conversation = conversationList.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).binding.tvHeader.setText(conversation.getSectionTitle());
        } else if (holder instanceof ConversationViewHolder) {
            ConversationViewHolder vh = (ConversationViewHolder) holder;
            vh.binding.tvUserName.setText(conversation.getName());

            if (conversation.getType() == ChatConversation.TYPE_GROUP) {
                vh.binding.tvUserEmail.setText(conversation.getGroupType()); // e.g., "CLASS", "CLUB"
            } else {
                vh.binding.tvUserEmail.setText(conversation.getEmail());
            }

            if (conversation.getImage() != null && !conversation.getImage().isEmpty()) {
                Glide.with(context)
                        .load(conversation.getImage())
                        .placeholder(R.drawable.logo)
                        .into(vh.binding.ivUserProfile);
            } else {
                vh.binding.ivUserProfile.setImageResource(R.drawable.logo);
            }

            vh.itemView.setOnClickListener(v -> {
                if (conversation.getType() == ChatConversation.TYPE_USER && isSearchMode) {
                    Intent intent = new Intent(context, I.imessenger.activities.UserProfileActivity.class);
                    intent.putExtra("userId", conversation.getId());
                    context.startActivity(intent);
                } else {
                    Intent intent = new Intent(context, ChatActivity.class);
                    if (conversation.getType() == ChatConversation.TYPE_GROUP) {
                        intent.putExtra("groupId", conversation.getId());
                    } else {
                        intent.putExtra("userId", conversation.getId());
                    }
                    intent.putExtra("userName", conversation.getName());
                    intent.putExtra("userImage", conversation.getImage());
                    context.startActivity(intent);
                }
            });
        }
    }

    private boolean isSearchMode = false;
    public void setSearchMode(boolean isSearchMode) {
        this.isSearchMode = isSearchMode;
    }

    @Override
    public int getItemCount() {
        return conversationList.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        ItemHeaderBinding binding;

        public HeaderViewHolder(@NonNull ItemHeaderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }

    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        ItemConversationModernBinding binding;

        public ConversationViewHolder(@NonNull ItemConversationModernBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
