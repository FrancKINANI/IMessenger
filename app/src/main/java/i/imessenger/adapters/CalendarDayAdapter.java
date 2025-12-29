package i.imessenger.adapters;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import i.imessenger.R;

public class CalendarDayAdapter extends RecyclerView.Adapter<CalendarDayAdapter.DayViewHolder> {

    private List<Integer> days = new ArrayList<>();
    private int selectedDay = -1;
    private int currentMonth;
    private int currentYear;
    private int todayDay = -1;
    private Set<Integer> daysWithEvents = new HashSet<>();
    private OnDayClickListener listener;

    public interface OnDayClickListener {
        void onDayClick(int day, int month, int year);
    }

    public CalendarDayAdapter(OnDayClickListener listener) {
        this.listener = listener;
    }

    public void setMonth(int year, int month) {
        this.currentYear = year;
        this.currentMonth = month;

        Calendar calendar = Calendar.getInstance();
        int today = calendar.get(Calendar.DAY_OF_MONTH);
        int todayMonth = calendar.get(Calendar.MONTH);
        int todayYear = calendar.get(Calendar.YEAR);

        if (year == todayYear && month == todayMonth) {
            this.todayDay = today;
            this.selectedDay = today;
        } else {
            this.todayDay = -1;
            this.selectedDay = 1;
        }

        days.clear();

        calendar.set(year, month, 1);
        int firstDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);

        // Add empty days for padding
        for (int i = 0; i < firstDayOfWeek; i++) {
            days.add(0);
        }

        // Add actual days
        for (int i = 1; i <= daysInMonth; i++) {
            days.add(i);
        }

        notifyDataSetChanged();
    }

    public void setDaysWithEvents(Set<Integer> daysWithEvents) {
        this.daysWithEvents = daysWithEvents;
        notifyDataSetChanged();
    }

    public void setSelectedDay(int day) {
        int oldSelected = selectedDay;
        selectedDay = day;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DayViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_calendar_day, parent, false);
        // Set width to 1/7 of parent
        ViewGroup.LayoutParams params = view.getLayoutParams();
        params.width = parent.getWidth() / 7;
        view.setLayoutParams(params);
        return new DayViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DayViewHolder holder, int position) {
        int day = days.get(position);

        if (day == 0) {
            holder.textDay.setText("");
            holder.dayContainer.setOnClickListener(null);
            holder.eventIndicator.setVisibility(View.INVISIBLE);
            return;
        }

        holder.textDay.setText(String.valueOf(day));

        // Style based on selection and today
        if (day == selectedDay) {
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setColor(Color.parseColor("#3B82F6"));
            holder.textDay.setBackground(background);
            holder.textDay.setTextColor(Color.WHITE);
        } else if (day == todayDay) {
            GradientDrawable background = new GradientDrawable();
            background.setShape(GradientDrawable.OVAL);
            background.setStroke(2, Color.parseColor("#3B82F6"));
            background.setColor(Color.TRANSPARENT);
            holder.textDay.setBackground(background);
            holder.textDay.setTextColor(Color.parseColor("#3B82F6"));
        } else {
            holder.textDay.setBackground(null);
            holder.textDay.setTextColor(Color.parseColor("#1E293B"));
        }

        // Event indicator
        if (daysWithEvents.contains(day)) {
            holder.eventIndicator.setVisibility(View.VISIBLE);
        } else {
            holder.eventIndicator.setVisibility(View.INVISIBLE);
        }

        holder.dayContainer.setOnClickListener(v -> {
            if (listener != null) {
                setSelectedDay(day);
                listener.onDayClick(day, currentMonth, currentYear);
            }
        });
    }

    @Override
    public int getItemCount() {
        return days.size();
    }

    static class DayViewHolder extends RecyclerView.ViewHolder {
        TextView textDay;
        View dayContainer;
        View eventIndicator;

        DayViewHolder(@NonNull View itemView) {
            super(itemView);
            textDay = itemView.findViewById(R.id.textDay);
            dayContainer = itemView.findViewById(R.id.dayContainer);
            eventIndicator = itemView.findViewById(R.id.eventIndicator);
        }
    }
}

