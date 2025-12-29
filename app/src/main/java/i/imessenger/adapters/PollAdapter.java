package i.imessenger.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import i.imessenger.R;
import i.imessenger.models.Poll;

public class PollAdapter extends RecyclerView.Adapter<PollAdapter.PollViewHolder> {

    private List<Poll> polls = new ArrayList<>();
    private String currentUserId;
    private OnPollVoteListener listener;

    public interface OnPollVoteListener {
        void onVote(Poll poll, String option);
    }

    public PollAdapter(String currentUserId, OnPollVoteListener listener) {
        this.currentUserId = currentUserId;
        this.listener = listener;
    }

    public void setPolls(List<Poll> polls) {
        this.polls = polls;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PollViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poll, parent, false);
        return new PollViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PollViewHolder holder, int position) {
        Poll poll = polls.get(position);

        holder.textCreatorName.setText(poll.getCreatorName());
        holder.textQuestion.setText(poll.getQuestion());
        holder.textTotalVotes.setText(poll.getTotalVotes() + " votes");

        // Time ago
        long diff = System.currentTimeMillis() - poll.getCreatedAt();
        String timeAgo;
        if (diff < TimeUnit.HOURS.toMillis(1)) {
            long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
            timeAgo = minutes + " minute" + (minutes != 1 ? "s" : "") + " ago";
        } else if (diff < TimeUnit.DAYS.toMillis(1)) {
            long hours = TimeUnit.MILLISECONDS.toHours(diff);
            timeAgo = hours + " hour" + (hours != 1 ? "s" : "") + " ago";
        } else {
            long days = TimeUnit.MILLISECONDS.toDays(diff);
            timeAgo = days + " day" + (days != 1 ? "s" : "") + " ago";
        }
        holder.textPollDate.setText(timeAgo);

        // Status
        if (poll.isActive()) {
            holder.textPollStatus.setText("Active");
            holder.textPollStatus.setTextColor(Color.parseColor("#10B981"));
        } else {
            holder.textPollStatus.setText("Closed");
            holder.textPollStatus.setTextColor(Color.parseColor("#64748B"));
        }

        // Check if user has voted
        boolean hasVoted = false;
        String votedOption = null;
        if (poll.getVotes() != null) {
            for (Map.Entry<String, List<String>> entry : poll.getVotes().entrySet()) {
                if (entry.getValue() != null && entry.getValue().contains(currentUserId)) {
                    hasVoted = true;
                    votedOption = entry.getKey();
                    break;
                }
            }
        }

        // Build options
        holder.layoutOptions.removeAllViews();
        if (poll.getOptions() != null) {
            int totalVotes = poll.getTotalVotes();

            for (String option : poll.getOptions()) {
                View optionView = LayoutInflater.from(holder.itemView.getContext())
                        .inflate(R.layout.item_poll_option, holder.layoutOptions, false);

                TextView textOptionText = optionView.findViewById(R.id.textOptionText);
                TextView textVotePercentage = optionView.findViewById(R.id.textVotePercentage);
                View progressBar = optionView.findViewById(R.id.progressBar);

                textOptionText.setText(option);

                // Calculate votes for this option
                int optionVotes = 0;
                if (poll.getVotes() != null && poll.getVotes().containsKey(option)) {
                    List<String> voters = poll.getVotes().get(option);
                    optionVotes = voters != null ? voters.size() : 0;
                }

                float percentage = totalVotes > 0 ? (optionVotes * 100f / totalVotes) : 0;
                textVotePercentage.setText(String.format("%.0f%%", percentage));

                // Set progress bar width
                final float finalPercentage = percentage;
                progressBar.post(() -> {
                    ViewGroup parent = (ViewGroup) progressBar.getParent();
                    int parentWidth = parent.getWidth();
                    ViewGroup.LayoutParams p = progressBar.getLayoutParams();
                    p.width = (int) (parentWidth * finalPercentage / 100);
                    progressBar.setLayoutParams(p);
                });

                // Highlight voted option
                final String opt = option;
                final boolean userVotedThis = option.equals(votedOption);
                if (userVotedThis) {
                    progressBar.setBackgroundColor(Color.parseColor("#3B82F6"));
                    textOptionText.setTextColor(Color.parseColor("#3B82F6"));
                } else {
                    progressBar.setBackgroundColor(Color.parseColor("#CBD5E1"));
                }

                // Click to vote
                final boolean finalHasVoted = hasVoted;
                if (poll.isActive() && !finalHasVoted) {
                    optionView.setOnClickListener(v -> {
                        if (listener != null) {
                            listener.onVote(poll, opt);
                        }
                    });
                }

                holder.layoutOptions.addView(optionView);
            }
        }
    }

    @Override
    public int getItemCount() {
        return polls.size();
    }

    static class PollViewHolder extends RecyclerView.ViewHolder {
        TextView textCreatorName, textPollDate, textPollStatus;
        TextView textQuestion, textTotalVotes;
        LinearLayout layoutOptions;

        PollViewHolder(@NonNull View itemView) {
            super(itemView);
            textCreatorName = itemView.findViewById(R.id.textCreatorName);
            textPollDate = itemView.findViewById(R.id.textPollDate);
            textPollStatus = itemView.findViewById(R.id.textPollStatus);
            textQuestion = itemView.findViewById(R.id.textQuestion);
            textTotalVotes = itemView.findViewById(R.id.textTotalVotes);
            layoutOptions = itemView.findViewById(R.id.layoutOptions);
        }
    }
}

