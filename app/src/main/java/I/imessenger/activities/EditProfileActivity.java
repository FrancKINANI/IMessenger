package I.imessenger.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.HashMap;
import java.util.Map;

import I.imessenger.R;
import I.imessenger.databinding.ActivityEditProfileBinding;
import I.imessenger.models.User;
import I.imessenger.viewmodels.ProfileViewModel;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private User currentUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        loadUserProfile();

        binding.btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);
        profileViewModel.getCurrentUser().observe(this, user -> {
            binding.progressBar.setVisibility(View.GONE);
            if (user != null) {
                currentUserModel = user;
                populateUserData(user);
            } else {
                Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(User user) {
        if (user != null) {
            binding.etFullName.setText(user.getFullName());
            binding.etRole.setText(user.getRole());
            binding.etLevel.setText(user.getLevel());
            binding.etGroups.setText(user.getGroups());
            
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.logo)
                        .into(binding.ivProfileImage);
            }
        }
    }

    private void saveUserProfile() {
        if (currentUserModel == null) return;

        String newName = binding.etFullName.getText().toString().trim();
        String newRole = binding.etRole.getText().toString().trim();
        String newLevel = binding.etLevel.getText().toString().trim();
        String newGroups = binding.etGroups.getText().toString().trim();

        binding.progressBar.setVisibility(View.VISIBLE);
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", newName);
        updates.put("role", newRole);
        updates.put("level", newLevel);
        updates.put("groups", newGroups);

        profileViewModel.updateUserProfile(currentUserModel.getUid(), updates).observe(this, success -> {
            binding.progressBar.setVisibility(View.GONE);
            if (success) {
                Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
