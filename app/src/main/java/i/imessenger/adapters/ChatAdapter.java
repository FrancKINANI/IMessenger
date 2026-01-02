package i.imessenger.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;

import java.util.List;

import i.imessenger.R;
import i.imessenger.databinding.ItemContainerReceivedMessageBinding;
import i.imessenger.databinding.ItemContainerSentMessageBinding;
import i.imessenger.models.ChatMessage;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public ChatAdapter(List<ChatMessage> chatMessages, String senderId) {
        this.chatMessages = chatMessages;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            return new SentMessageViewHolder(
                    ItemContainerSentMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        } else {
            return new ReceivedMessageViewHolder(
                    ItemContainerReceivedMessageBinding.inflate(
                            LayoutInflater.from(parent.getContext()),
                            parent,
                            false
                    )
            );
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (getItemViewType(position) == VIEW_TYPE_SENT) {
            ((SentMessageViewHolder) holder).setData(chatMessages.get(position));
        } else {
            ((ReceivedMessageViewHolder) holder).setData(chatMessages.get(position));
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage message = chatMessages.get(position);
        if (message.senderId != null && message.senderId.equals(senderId)) {
            return VIEW_TYPE_SENT;
        } else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerSentMessageBinding binding;

        SentMessageViewHolder(ItemContainerSentMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage) {
            Context context = binding.getRoot().getContext();

            // Handle text message
            if (chatMessage.message != null && !chatMessage.message.isEmpty()) {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setText(chatMessage.message);
            } else {
                binding.textMessage.setVisibility(View.GONE);
            }

            binding.textDateTime.setText(chatMessage.dateTime);

            // Handle media
            if (chatMessage.hasMedia()) {
                binding.mediaContainer.setVisibility(View.VISIBLE);

                List<String> mediaUrls = chatMessage.mediaUrls;
                List<String> mediaTypes = chatMessage.mediaTypes;
                List<String> mediaNames = chatMessage.mediaNames;

                if (mediaUrls.size() == 1) {
                    // Single media - show larger
                    binding.singleMediaContainer.setVisibility(View.VISIBLE);
                    binding.mediaRecyclerView.setVisibility(View.GONE);

                    String url = mediaUrls.get(0);
                    String type = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.get(0) : "image";
                    String name = mediaNames != null && !mediaNames.isEmpty() ? mediaNames.get(0) : "file";

                    if ("document".equals(type)) {
                        // Show document with icon
                        binding.singleMediaImage.setImageResource(R.drawable.ic_document);
                        binding.singleMediaImage.setScaleType(android.widget.ImageView.ScaleType.CENTER);
                        binding.videoPlayIcon.setVisibility(View.GONE);
                        binding.singleMediaContainer.setOnClickListener(v -> {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(url), "*/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        binding.singleMediaImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        Glide.with(context)
                                .load(url)
                                .transform(new CenterCrop(), new RoundedCorners(16))
                                .into(binding.singleMediaImage);

                        if ("video".equals(type)) {
                            binding.videoPlayIcon.setVisibility(View.VISIBLE);
                            binding.singleMediaContainer.setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(url), "video/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            binding.videoPlayIcon.setVisibility(View.GONE);
                            binding.singleMediaContainer.setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(url), "image/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                } else {
                    // Multiple media - show grid
                    binding.singleMediaContainer.setVisibility(View.GONE);
                    binding.mediaRecyclerView.setVisibility(View.VISIBLE);

                    ChatMediaGridAdapter adapter = new ChatMediaGridAdapter(mediaUrls, mediaTypes, mediaNames);
                    binding.mediaRecyclerView.setAdapter(adapter);
                }
            } else {
                binding.mediaContainer.setVisibility(View.GONE);
            }
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemContainerReceivedMessageBinding binding;

        ReceivedMessageViewHolder(ItemContainerReceivedMessageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void setData(ChatMessage chatMessage) {
            Context context = binding.getRoot().getContext();

            // Handle text message
            if (chatMessage.message != null && !chatMessage.message.isEmpty()) {
                binding.textMessage.setVisibility(View.VISIBLE);
                binding.textMessage.setText(chatMessage.message);
            } else {
                binding.textMessage.setVisibility(View.GONE);
            }

            binding.textDateTime.setText(chatMessage.dateTime);

            // Handle media
            if (chatMessage.hasMedia()) {
                binding.mediaContainer.setVisibility(View.VISIBLE);

                List<String> mediaUrls = chatMessage.mediaUrls;
                List<String> mediaTypes = chatMessage.mediaTypes;
                List<String> mediaNames = chatMessage.mediaNames;

                if (mediaUrls.size() == 1) {
                    // Single media - show larger
                    binding.singleMediaContainer.setVisibility(View.VISIBLE);
                    binding.mediaRecyclerView.setVisibility(View.GONE);

                    String url = mediaUrls.get(0);
                    String type = mediaTypes != null && !mediaTypes.isEmpty() ? mediaTypes.get(0) : "image";
                    String name = mediaNames != null && !mediaNames.isEmpty() ? mediaNames.get(0) : "file";

                    if ("document".equals(type)) {
                        // Show document with icon
                        binding.singleMediaImage.setImageResource(R.drawable.ic_document);
                        binding.singleMediaImage.setScaleType(android.widget.ImageView.ScaleType.CENTER);
                        binding.videoPlayIcon.setVisibility(View.GONE);
                        binding.singleMediaContainer.setOnClickListener(v -> {
                            try {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setDataAndType(Uri.parse(url), "*/*");
                                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                context.startActivity(intent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    } else {
                        binding.singleMediaImage.setScaleType(android.widget.ImageView.ScaleType.CENTER_CROP);
                        Glide.with(context)
                                .load(url)
                                .transform(new CenterCrop(), new RoundedCorners(16))
                                .into(binding.singleMediaImage);

                        if ("video".equals(type)) {
                            binding.videoPlayIcon.setVisibility(View.VISIBLE);
                            binding.singleMediaContainer.setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(url), "video/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        } else {
                            binding.videoPlayIcon.setVisibility(View.GONE);
                            binding.singleMediaContainer.setOnClickListener(v -> {
                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW);
                                    intent.setDataAndType(Uri.parse(url), "image/*");
                                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                    context.startActivity(intent);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            });
                        }
                    }
                } else {
                    // Multiple media - show grid
                    binding.singleMediaContainer.setVisibility(View.GONE);
                    binding.mediaRecyclerView.setVisibility(View.VISIBLE);

                    ChatMediaGridAdapter adapter = new ChatMediaGridAdapter(mediaUrls, mediaTypes, mediaNames);
                    binding.mediaRecyclerView.setAdapter(adapter);
                }
            } else {
                binding.mediaContainer.setVisibility(View.GONE);
            }
        }
    }
}
