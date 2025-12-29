package i.imessenger.fragments;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import i.imessenger.R;
import i.imessenger.adapters.CalendarDayAdapter;
import i.imessenger.adapters.EventAdapter;
import i.imessenger.databinding.FragmentCalendarBinding;
import i.imessenger.models.CalendarEvent;
import i.imessenger.models.User;

public class CalendarFragment extends Fragment implements CalendarDayAdapter.OnDayClickListener, EventAdapter.OnEventClickListener {

    private FragmentCalendarBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CalendarDayAdapter calendarAdapter;
    private EventAdapter eventAdapter;

    private int currentYear;
    private int currentMonth;
    private int selectedDay;
    private String currentUserId;
    private String userRole = "student";
    private String userLevel = "";
    private String userName = "";

    private SimpleDateFormat monthYearFormat = new SimpleDateFormat("MMMM yyyy", Locale.getDefault());
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    private String selectedColor = "#3B82F6";
    private long selectedStartTime = 0;
    private long selectedEndTime = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        setupToolbar();
        setupCalendar();
        setupEventsList();
        loadUserInfo();

        binding.fabAddEvent.setOnClickListener(v -> showAddEventDialog());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupCalendar() {
        Calendar calendar = Calendar.getInstance();
        currentYear = calendar.get(Calendar.YEAR);
        currentMonth = calendar.get(Calendar.MONTH);
        selectedDay = calendar.get(Calendar.DAY_OF_MONTH);

        calendarAdapter = new CalendarDayAdapter(this);
        binding.recyclerCalendarDays.setLayoutManager(new GridLayoutManager(getContext(), 7));
        binding.recyclerCalendarDays.setAdapter(calendarAdapter);

        updateCalendarView();

        binding.btnPreviousMonth.setOnClickListener(v -> {
            currentMonth--;
            if (currentMonth < 0) {
                currentMonth = 11;
                currentYear--;
            }
            updateCalendarView();
        });

        binding.btnNextMonth.setOnClickListener(v -> {
            currentMonth++;
            if (currentMonth > 11) {
                currentMonth = 0;
                currentYear++;
            }
            updateCalendarView();
        });
    }

    private void updateCalendarView() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(currentYear, currentMonth, 1);
        binding.textCurrentMonth.setText(monthYearFormat.format(calendar.getTime()));
        calendarAdapter.setMonth(currentYear, currentMonth);
        loadEventsForMonth();
    }

    private void setupEventsList() {
        eventAdapter = new EventAdapter(currentUserId, userRole, this);
        binding.recyclerEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerEvents.setAdapter(eventAdapter);
    }

    private void loadUserInfo() {
        if (currentUserId.isEmpty()) return;

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            userRole = user.getRole() != null ? user.getRole() : "student";
                            userLevel = user.getLevel() != null ? user.getLevel() : "";
                            userName = user.getFullName() != null ? user.getFullName() : "";
                            eventAdapter = new EventAdapter(currentUserId, userRole, this);
                            binding.recyclerEvents.setAdapter(eventAdapter);
                            loadEventsForDay(selectedDay, currentMonth, currentYear);
                        }
                    }
                });
    }

    private void loadEventsForMonth() {
        Calendar startCal = Calendar.getInstance();
        startCal.set(currentYear, currentMonth, 1, 0, 0, 0);
        long monthStart = startCal.getTimeInMillis();

        Calendar endCal = Calendar.getInstance();
        endCal.set(currentYear, currentMonth, startCal.getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59);
        long monthEnd = endCal.getTimeInMillis();

        db.collection("events")
                .whereGreaterThanOrEqualTo("startTime", monthStart)
                .whereLessThanOrEqualTo("startTime", monthEnd)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    Set<Integer> daysWithEvents = new HashSet<>();
                    Calendar cal = Calendar.getInstance();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CalendarEvent event = doc.toObject(CalendarEvent.class);
                        if (canViewEvent(event)) {
                            cal.setTimeInMillis(event.getStartTime());
                            daysWithEvents.add(cal.get(Calendar.DAY_OF_MONTH));
                        }
                    }

                    calendarAdapter.setDaysWithEvents(daysWithEvents);
                });
    }

    private boolean canViewEvent(CalendarEvent event) {
        if (event == null) return false;

        if ("PERSONAL".equals(event.getEventType())) {
            return currentUserId.equals(event.getCreatorId());
        }

        if ("SCHOOL".equals(event.getEventType())) {
            return true;
        }

        if ("CLASS".equals(event.getEventType())) {
            if (userRole.equalsIgnoreCase("admin") || userRole.equalsIgnoreCase("teacher")) {
                return true;
            }
            return userLevel != null && userLevel.equals(event.getTargetClass());
        }

        return true;
    }

    @Override
    public void onDayClick(int day, int month, int year) {
        selectedDay = day;
        loadEventsForDay(day, month, year);
    }

    private void loadEventsForDay(int day, int month, int year) {
        Calendar startCal = Calendar.getInstance();
        startCal.set(year, month, day, 0, 0, 0);
        long dayStart = startCal.getTimeInMillis();

        Calendar endCal = Calendar.getInstance();
        endCal.set(year, month, day, 23, 59, 59);
        long dayEnd = endCal.getTimeInMillis();

        binding.textSelectedDate.setText("Events for " + dateFormat.format(startCal.getTime()));

        db.collection("events")
                .whereGreaterThanOrEqualTo("startTime", dayStart)
                .whereLessThanOrEqualTo("startTime", dayEnd)
                .orderBy("startTime", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<CalendarEvent> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        CalendarEvent event = doc.toObject(CalendarEvent.class);
                        if (canViewEvent(event)) {
                            events.add(event);
                        }
                    }

                    eventAdapter.setEvents(events);
                    binding.textNoEventsForDay.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showAddEventDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_event, null);

        TextInputEditText editTitle = dialogView.findViewById(R.id.editEventTitle);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editEventDescription);
        TextInputEditText editStartDate = dialogView.findViewById(R.id.editStartDate);
        TextInputEditText editStartTime = dialogView.findViewById(R.id.editStartTime);
        TextInputEditText editEndDate = dialogView.findViewById(R.id.editEndDate);
        TextInputEditText editEndTime = dialogView.findViewById(R.id.editEndTime);
        View layoutEventType = dialogView.findViewById(R.id.layoutEventType);
        RadioGroup radioGroupEventType = dialogView.findViewById(R.id.radioGroupEventType);
        View layoutTargetClass = dialogView.findViewById(R.id.layoutTargetClass);
        AutoCompleteTextView spinnerTargetClass = dialogView.findViewById(R.id.spinnerTargetClass);

        View colorBlue = dialogView.findViewById(R.id.colorBlue);
        View colorGreen = dialogView.findViewById(R.id.colorGreen);
        View colorOrange = dialogView.findViewById(R.id.colorOrange);
        View colorRed = dialogView.findViewById(R.id.colorRed);
        View colorPurple = dialogView.findViewById(R.id.colorPurple);

        selectedColor = "#3B82F6";
        View.OnClickListener colorClickListener = v -> {
            colorBlue.setAlpha(0.5f);
            colorGreen.setAlpha(0.5f);
            colorOrange.setAlpha(0.5f);
            colorRed.setAlpha(0.5f);
            colorPurple.setAlpha(0.5f);
            v.setAlpha(1f);

            if (v == colorBlue) selectedColor = "#3B82F6";
            else if (v == colorGreen) selectedColor = "#10B981";
            else if (v == colorOrange) selectedColor = "#F59E0B";
            else if (v == colorRed) selectedColor = "#EF4444";
            else if (v == colorPurple) selectedColor = "#8B5CF6";
        };

        colorBlue.setOnClickListener(colorClickListener);
        colorGreen.setOnClickListener(colorClickListener);
        colorOrange.setOnClickListener(colorClickListener);
        colorRed.setOnClickListener(colorClickListener);
        colorPurple.setOnClickListener(colorClickListener);

        if (userRole.equalsIgnoreCase("admin") || userRole.equalsIgnoreCase("teacher")) {
            layoutEventType.setVisibility(View.VISIBLE);

            String[] classes = {"Level 1", "Level 2", "Level 3", "Level 4", "Level 5"};
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_dropdown_item_1line, classes);
            spinnerTargetClass.setAdapter(adapter);

            radioGroupEventType.setOnCheckedChangeListener((group, checkedId) -> {
                layoutTargetClass.setVisibility(checkedId == R.id.radioClass ? View.VISIBLE : View.GONE);
            });
        }

        Calendar defaultCal = Calendar.getInstance();
        defaultCal.set(currentYear, currentMonth, selectedDay);
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
        SimpleDateFormat stf = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        editStartDate.setText(sdf.format(defaultCal.getTime()));
        editEndDate.setText(sdf.format(defaultCal.getTime()));

        defaultCal.set(Calendar.HOUR_OF_DAY, 9);
        defaultCal.set(Calendar.MINUTE, 0);
        selectedStartTime = defaultCal.getTimeInMillis();
        editStartTime.setText(stf.format(defaultCal.getTime()));

        defaultCal.set(Calendar.HOUR_OF_DAY, 10);
        selectedEndTime = defaultCal.getTimeInMillis();
        editEndTime.setText(stf.format(defaultCal.getTime()));

        editStartDate.setOnClickListener(v -> showDatePicker((date, cal) -> {
            editStartDate.setText(sdf.format(date));
            Calendar startCal = Calendar.getInstance();
            startCal.setTimeInMillis(selectedStartTime);
            startCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            selectedStartTime = startCal.getTimeInMillis();
        }));

        editStartTime.setOnClickListener(v -> showTimePicker((hour, minute) -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedStartTime);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            selectedStartTime = cal.getTimeInMillis();
            editStartTime.setText(stf.format(cal.getTime()));
        }));

        editEndDate.setOnClickListener(v -> showDatePicker((date, cal) -> {
            editEndDate.setText(sdf.format(date));
            Calendar endCal = Calendar.getInstance();
            endCal.setTimeInMillis(selectedEndTime);
            endCal.set(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
            selectedEndTime = endCal.getTimeInMillis();
        }));

        editEndTime.setOnClickListener(v -> showTimePicker((hour, minute) -> {
            Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(selectedEndTime);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            selectedEndTime = cal.getTimeInMillis();
            editEndTime.setText(stf.format(cal.getTime()));
        }));

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnSave).setOnClickListener(v -> {
            String title = editTitle.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            if (title.isEmpty()) {
                editTitle.setError("Title is required");
                return;
            }

            String eventType = "PERSONAL";
            String targetClass = null;

            if (userRole.equalsIgnoreCase("admin") || userRole.equalsIgnoreCase("teacher")) {
                int checkedId = radioGroupEventType.getCheckedRadioButtonId();
                if (checkedId == R.id.radioClass) {
                    eventType = "CLASS";
                    targetClass = spinnerTargetClass.getText().toString();
                    if (targetClass.isEmpty()) {
                        Toast.makeText(getContext(), "Please select a target class", Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else if (checkedId == R.id.radioSchool) {
                    eventType = "SCHOOL";
                }
            }

            String eventId = UUID.randomUUID().toString();
            CalendarEvent event = new CalendarEvent(
                    eventId, title, description, selectedStartTime, selectedEndTime,
                    currentUserId, userName, eventType, targetClass, null,
                    selectedColor, false, null
            );

            db.collection("events").document(eventId)
                    .set(event)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Event created", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadEventsForMonth();
                        loadEventsForDay(selectedDay, currentMonth, currentYear);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to create event", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    private void showDatePicker(DatePickerCallback callback) {
        Calendar cal = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            cal.set(year, month, dayOfMonth);
            callback.onDateSelected(cal.getTime(), cal);
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showTimePicker(TimePickerCallback callback) {
        Calendar cal = Calendar.getInstance();
        new TimePickerDialog(getContext(), (view, hourOfDay, minute) -> {
            callback.onTimeSelected(hourOfDay, minute);
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), false).show();
    }

    interface DatePickerCallback {
        void onDateSelected(Date date, Calendar calendar);
    }

    interface TimePickerCallback {
        void onTimeSelected(int hour, int minute);
    }

    @Override
    public void onEventClick(CalendarEvent event) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle(event.getTitle())
                .setMessage(event.getDescription() != null ? event.getDescription() : "No description")
                .setPositiveButton("OK", null)
                .show();
    }

    @Override
    public void onEventDelete(CalendarEvent event) {
        new MaterialAlertDialogBuilder(getContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("events").document(event.getEventId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                                loadEventsForMonth();
                                loadEventsForDay(selectedDay, currentMonth, currentYear);
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

