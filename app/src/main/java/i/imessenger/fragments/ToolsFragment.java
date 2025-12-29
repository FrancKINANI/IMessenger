package i.imessenger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.EventAdapter;
import i.imessenger.databinding.FragmentToolsBinding;
import i.imessenger.models.CalendarEvent;
import i.imessenger.models.User;

public class ToolsFragment extends Fragment {

    private FragmentToolsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private EventAdapter upcomingEventsAdapter;

    private String currentUserId;
    private String userRole = "student";
    private String userLevel = "";

    public ToolsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentToolsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        setupClickListeners();
        setupUpcomingEvents();
        loadUserInfo();
    }

    private void setupClickListeners() {
        binding.cardCalendar.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_nav_tools_to_calendarFragment)
        );

        binding.cardProjects.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_nav_tools_to_projectsFragment)
        );

        binding.cardPolls.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_nav_tools_to_pollsFragment)
        );

        binding.cardMore.setOnClickListener(v ->
            Toast.makeText(getContext(), "More tools coming soon!", Toast.LENGTH_SHORT).show()
        );
    }

    private void setupUpcomingEvents() {
        upcomingEventsAdapter = new EventAdapter(currentUserId, userRole, new EventAdapter.OnEventClickListener() {
            @Override
            public void onEventClick(CalendarEvent event) {
                // Navigate to calendar
                Navigation.findNavController(requireView()).navigate(R.id.action_nav_tools_to_calendarFragment);
            }

            @Override
            public void onEventDelete(CalendarEvent event) {
                // Not allowed from here
            }
        });

        binding.recyclerUpcomingEvents.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerUpcomingEvents.setAdapter(upcomingEventsAdapter);
    }

    private void loadUserInfo() {
        if (currentUserId.isEmpty()) {
            loadUpcomingEvents();
            return;
        }

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        userRole = user.getRole() != null ? user.getRole() : "student";
                        userLevel = user.getLevel() != null ? user.getLevel() : "";
                        upcomingEventsAdapter = new EventAdapter(currentUserId, userRole, new EventAdapter.OnEventClickListener() {
                            @Override
                            public void onEventClick(CalendarEvent event) {
                                Navigation.findNavController(requireView()).navigate(R.id.action_nav_tools_to_calendarFragment);
                            }

                            @Override
                            public void onEventDelete(CalendarEvent event) {}
                        });
                        binding.recyclerUpcomingEvents.setAdapter(upcomingEventsAdapter);
                    }
                }
                loadUpcomingEvents();
            })
            .addOnFailureListener(e -> loadUpcomingEvents());
    }

    private void loadUpcomingEvents() {
        long now = System.currentTimeMillis();
        Calendar endCal = Calendar.getInstance();
        endCal.add(Calendar.DAY_OF_MONTH, 7); // Next 7 days
        long weekEnd = endCal.getTimeInMillis();

        db.collection("events")
            .whereGreaterThanOrEqualTo("startTime", now)
            .whereLessThanOrEqualTo("startTime", weekEnd)
            .orderBy("startTime", Query.Direction.ASCENDING)
            .limit(5)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<CalendarEvent> events = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    CalendarEvent event = doc.toObject(CalendarEvent.class);
                    if (canViewEvent(event)) {
                        events.add(event);
                    }
                }

                upcomingEventsAdapter.setEvents(events);
                binding.textNoEvents.setVisibility(events.isEmpty() ? View.VISIBLE : View.GONE);
                binding.recyclerUpcomingEvents.setVisibility(events.isEmpty() ? View.GONE : View.VISIBLE);
            })
            .addOnFailureListener(e -> {
                binding.textNoEvents.setVisibility(View.VISIBLE);
                binding.recyclerUpcomingEvents.setVisibility(View.GONE);
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
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
