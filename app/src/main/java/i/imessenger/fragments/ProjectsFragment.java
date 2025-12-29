package i.imessenger.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import i.imessenger.R;
import i.imessenger.adapters.ProjectAdapter;
import i.imessenger.databinding.FragmentProjectsBinding;
import i.imessenger.models.Project;
import i.imessenger.models.User;

public class ProjectsFragment extends Fragment implements ProjectAdapter.OnProjectClickListener {

    private FragmentProjectsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ProjectAdapter adapter;

    private String currentUserId;
    private String userName = "";
    private String currentFilter = "PROJECT"; // PROJECT, CLUB, STUDY_GROUP

    private long selectedDeadline = 0;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProjectsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        setupToolbar();
        setupTabs();
        setupRecyclerView();
        loadUserInfo();

        binding.fabAddProject.setOnClickListener(v -> showAddProjectDialog());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
            Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupTabs() {
        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        currentFilter = "PROJECT";
                        break;
                    case 1:
                        currentFilter = "CLUB";
                        break;
                    case 2:
                        currentFilter = "STUDY_GROUP";
                        break;
                }
                loadProjects();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void setupRecyclerView() {
        adapter = new ProjectAdapter(this);
        binding.recyclerProjects.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerProjects.setAdapter(adapter);
    }

    private void loadUserInfo() {
        if (currentUserId.isEmpty()) return;

        db.collection("users").document(currentUserId)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    User user = doc.toObject(User.class);
                    if (user != null) {
                        userName = user.getFullName() != null ? user.getFullName() : "";
                    }
                }
                loadProjects();
            });
    }

    private void loadProjects() {
        db.collection("projects")
            .whereEqualTo("projectType", currentFilter)
            .whereArrayContains("members", currentUserId)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<Project> projects = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    Project project = doc.toObject(Project.class);
                    projects.add(project);
                }

                adapter.setProjects(projects);
                binding.textNoProjects.setVisibility(projects.isEmpty() ? View.VISIBLE : View.GONE);
            });
    }

    private void showAddProjectDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_project, null);

        TextInputEditText editName = dialogView.findViewById(R.id.editProjectName);
        TextInputEditText editDescription = dialogView.findViewById(R.id.editProjectDescription);
        TextInputEditText editDeadline = dialogView.findViewById(R.id.editDeadline);
        ChipGroup chipGroup = dialogView.findViewById(R.id.chipGroupProjectType);

        selectedDeadline = 0;

        editDeadline.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
                cal.set(year, month, dayOfMonth, 23, 59, 59);
                selectedDeadline = cal.getTimeInMillis();
                editDeadline.setText(dateFormat.format(cal.getTime()));
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show();
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
            .setView(dialogView)
            .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String name = editName.getText().toString().trim();
            String description = editDescription.getText().toString().trim();

            if (name.isEmpty()) {
                editName.setError("Name is required");
                return;
            }

            if (description.isEmpty()) {
                editDescription.setError("Description is required");
                return;
            }

            String projectType;
            int checkedId = chipGroup.getCheckedChipId();
            if (checkedId == R.id.chipClub) {
                projectType = "CLUB";
            } else if (checkedId == R.id.chipStudyGroup) {
                projectType = "STUDY_GROUP";
            } else {
                projectType = "PROJECT";
            }

            final String finalProjectType = projectType;

            String projectId = UUID.randomUUID().toString();
            List<String> members = new ArrayList<>();
            members.add(currentUserId);
            List<String> admins = new ArrayList<>();
            admins.add(currentUserId);

            Project project = new Project(
                projectId,
                name,
                description,
                projectType,
                currentUserId,
                userName,
                members,
                admins,
                null,
                selectedDeadline
            );

            db.collection("projects").document(projectId)
                .set(project)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Project created", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();

                    // Switch to the appropriate tab
                    switch (finalProjectType) {
                        case "PROJECT":
                            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(0));
                            break;
                        case "CLUB":
                            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(1));
                            break;
                        case "STUDY_GROUP":
                            binding.tabLayout.selectTab(binding.tabLayout.getTabAt(2));
                            break;
                    }
                    loadProjects();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create project", Toast.LENGTH_SHORT).show();
                });
        });

        dialog.show();
    }

    @Override
    public void onProjectClick(Project project) {
        // Show project details dialog
        StringBuilder details = new StringBuilder();
        details.append("Type: ").append(getProjectTypeDisplay(project.getProjectType())).append("\n\n");
        details.append("Description:\n").append(project.getDescription()).append("\n\n");
        details.append("Members: ").append(project.getMembers() != null ? project.getMembers().size() : 0).append("\n");
        details.append("Status: ").append(project.getStatus()).append("\n");

        if (project.getDeadline() > 0) {
            details.append("Deadline: ").append(dateFormat.format(new Date(project.getDeadline())));
        }

        boolean isAdmin = project.getAdmins() != null && project.getAdmins().contains(currentUserId);

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getContext())
            .setTitle(project.getName())
            .setMessage(details.toString())
            .setPositiveButton("Close", null);

        if (isAdmin) {
            builder.setNeutralButton("Delete", (dialog, which) -> {
                new MaterialAlertDialogBuilder(getContext())
                    .setTitle("Delete Project")
                    .setMessage("Are you sure you want to delete this project?")
                    .setPositiveButton("Delete", (d, w) -> {
                        db.collection("projects").document(project.getProjectId())
                            .delete()
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(getContext(), "Project deleted", Toast.LENGTH_SHORT).show();
                                loadProjects();
                            });
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            });
        }

        builder.show();
    }

    private String getProjectTypeDisplay(String type) {
        if (type == null) return "Project";
        switch (type) {
            case "CLUB": return "Club";
            case "STUDY_GROUP": return "Study Group";
            default: return "Project";
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
