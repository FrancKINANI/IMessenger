package I.imessenger.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import I.imessenger.R;
import I.imessenger.databinding.ActivityEditProfileBinding;
import I.imessenger.models.User;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private FirebaseFirestore db;
    private User currentUserModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
            getSupportActionBar().setTitle("Edit Profile");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        loadUserProfile();

        binding.btnSave.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            binding.progressBar.setVisibility(View.VISIBLE);
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        binding.progressBar.setVisibility(View.GONE);
                        if (documentSnapshot.exists()) {
                            currentUserModel = documentSnapshot.toObject(User.class);
                            populateUserData(currentUserModel);
                        }
                    })
                    .addOnFailureListener(e -> {
                        binding.progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                    });
        }
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

        db.collection("users").document(currentUserModel.getUid())
                .update(updates)
                .addOnSuccessListener(unused -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Profile Updated", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
                });
    }
}
