package i.imessenger.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import i.imessenger.R;
import i.imessenger.models.CalendarEvent;

public class EventAdapter extends RecyclerView.Adapter<EventAdapter.EventViewHolder> {

    private List<CalendarEvent> events = new ArrayList<>();
    private String currentUserId;
    private String userRole;
    private OnEventClickListener listener;
    private SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());

    public interface OnEventClickListener {
        void onEventClick(CalendarEvent event);
        void onEventDelete(CalendarEvent event);
    }

    public EventAdapter(String currentUserId, String userRole, OnEventClickListener listener) {
        this.currentUserId = currentUserId;
        this.userRole = userRole;
        this.listener = listener;
    }

    public void setEvents(List<CalendarEvent> events) {
        this.events = events;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventViewHolder holder, int position) {
        CalendarEvent event = events.get(position);

        holder.textEventTitle.setText(event.getTitle());

        String startTime = timeFormat.format(new Date(event.getStartTime()));
        String endTime = timeFormat.format(new Date(event.getEndTime()));
        holder.textEventTime.setText(startTime + " - " + endTime);

        if (event.getDescription() != null && !event.getDescription().isEmpty()) {
            holder.textEventDescription.setText(event.getDescription());
            holder.textEventDescription.setVisibility(View.VISIBLE);
        } else {
            holder.textEventDescription.setVisibility(View.GONE);
        }

        // Color indicator
        if (event.getColor() != null) {
            try {
                holder.colorIndicator.setBackgroundColor(Color.parseColor(event.getColor()));
            } catch (Exception e) {
                holder.colorIndicator.setBackgroundColor(Color.parseColor("#3B82F6"));
            }
        }

        // Event type badge
        if (event.getEventType() != null && !event.getEventType().equals("PERSONAL")) {
            holder.layoutEventType.setVisibility(View.VISIBLE);
            if (event.getEventType().equals("CLASS")) {
                holder.textEventType.setText("Class: " + (event.getTargetClass() != null ? event.getTargetClass() : ""));
            } else if (event.getEventType().equals("SCHOOL")) {
                holder.textEventType.setText("School Event");
            }
        } else {
            holder.layoutEventType.setVisibility(View.GONE);
        }

        // Show delete button for event creator or admins/teachers
        boolean canDelete = event.getCreatorId() != null && event.getCreatorId().equals(currentUserId);
        if (userRole != null && (userRole.equalsIgnoreCase("admin") || userRole.equalsIgnoreCase("teacher"))) {
            canDelete = true;
        }
        holder.btnDeleteEvent.setVisibility(canDelete ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventClick(event);
            }
        });

        holder.btnDeleteEvent.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEventDelete(event);
            }
        });
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventViewHolder extends RecyclerView.ViewHolder {
        TextView textEventTitle, textEventTime, textEventDescription, textEventType;
        View colorIndicator, layoutEventType;
        ImageButton btnDeleteEvent;

        EventViewHolder(@NonNull View itemView) {
            super(itemView);
            textEventTitle = itemView.findViewById(R.id.textEventTitle);
            textEventTime = itemView.findViewById(R.id.textEventTime);
            textEventDescription = itemView.findViewById(R.id.textEventDescription);
            textEventType = itemView.findViewById(R.id.textEventType);
            colorIndicator = itemView.findViewById(R.id.colorIndicator);
            layoutEventType = itemView.findViewById(R.id.layoutEventType);
            btnDeleteEvent = itemView.findViewById(R.id.btnDeleteEvent);
        }
    }
}

