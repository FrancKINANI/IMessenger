package i.imessenger.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import androidx.navigation.Navigation;
import android.os.Bundle;

import com.bumptech.glide.Glide;

import java.util.List;

import i.imessenger.R;
import i.imessenger.databinding.ItemHeaderBinding;
import i.imessenger.databinding.ItemConversationModernBinding;
import i.imessenger.models.ChatConversation;

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
                    Bundle bundle = new Bundle();
                    bundle.putString("userId", conversation.getId());
                    Navigation.findNavController(v).navigate(R.id.userProfileFragment, bundle);
                } else {
                    Bundle bundle = new Bundle();
                    if (conversation.getType() == ChatConversation.TYPE_GROUP) {
                        bundle.putString("groupId", conversation.getId());
                    } else {
                        bundle.putString("userId", conversation.getId());
                    }
                    bundle.putString("userName", conversation.getName());
                    bundle.putString("userImage", conversation.getImage());
                    Navigation.findNavController(v).navigate(R.id.chatFragment, bundle);
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
