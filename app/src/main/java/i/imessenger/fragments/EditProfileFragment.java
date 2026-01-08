package i.imessenger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import java.util.HashMap;
import java.util.Map;

import i.imessenger.R;
import i.imessenger.databinding.FragmentEditProfileBinding;
import i.imessenger.models.User;
import i.imessenger.viewmodels.ProfileViewModel;

public class EditProfileFragment extends Fragment {

    private FragmentEditProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private User currentUserModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentEditProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        // Toolbar setup done via XML usually or here
        // Assuming toolbar is in layout
        if (binding.toolbar != null) {
            binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());
            binding.toolbar.setTitle("Edit Profile");
        }

        loadUserProfile();

        binding.btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);
        profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
            binding.progressBar.setVisibility(View.GONE);
            if (user != null) {
                currentUserModel = user;
                populateUserData(user);
            } else {
                Toast.makeText(getContext(), "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(User user) {
        if (user != null) {
            binding.etFullName.setText(user.getFullName());
            binding.etRole.setText(user.getRole());
            binding.etLevel.setText(user.getLevel());
            binding.etGroups.setText(user.getGroups());

            // Mini-CV
            if (user.getSkills() != null) {
                binding.etSkills.setText(android.text.TextUtils.join(", ", user.getSkills()));
            }
            binding.etLinkedin.setText(user.getLinkedin());
            binding.etPortfolio.setText(user.getPortfolio());

            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.logo)
                        .into(binding.ivProfileImage);
            }
        }
    }

    private void saveUserProfile() {
        if (currentUserModel == null)
            return;

        String newName = binding.etFullName.getText().toString().trim();
        String newRole = binding.etRole.getText().toString().trim();
        String newLevel = binding.etLevel.getText().toString().trim();
        String newGroups = binding.etGroups.getText().toString().trim();

        String skillsStr = binding.etSkills.getText().toString().trim();
        String linkedin = binding.etLinkedin.getText().toString().trim();
        String portfolio = binding.etPortfolio.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        updates.put("role", newRole);
        updates.put("level", newLevel);
        updates.put("groups", newGroups);

        java.util.List<String> skillsList = new java.util.ArrayList<>();
        if (!skillsStr.isEmpty()) {
            for (String s : skillsStr.split(",")) {
                if (!s.trim().isEmpty()) {
                    skillsList.add(s.trim());
                }
            }
        }
        updates.put("skills", skillsList);
        updates.put("linkedin", linkedin);
        updates.put("portfolio", portfolio);

        profileViewModel.updateUserProfile(currentUserModel.getUid(), updates).observe(getViewLifecycleOwner(),
                success -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (success) {
                        Toast.makeText(getContext(), "Profile Updated", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this).navigateUp();
                    } else {
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
