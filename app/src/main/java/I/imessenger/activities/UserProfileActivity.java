package I.imessenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import I.imessenger.R;
import I.imessenger.databinding.ActivityUserProfileBinding;
import I.imessenger.models.User;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private FirebaseFirestore db;
    private User userModel;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityUserProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userId = getIntent().getStringExtra("userId");
        if (userId == null) {
            finish();
            return;
        }

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        db = FirebaseFirestore.getInstance();
        loadUserProfile();

        binding.btnMessage.setOnClickListener(v -> {
            if (userModel != null) {
                Intent intent = new Intent(this, ChatActivity.class);
                intent.putExtra("userId", userModel.getUid());
                intent.putExtra("userName", userModel.getFullName());
                intent.putExtra("userImage", userModel.getProfileImage());
                startActivity(intent);
            }
        });
    }

    private void loadUserProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    binding.progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        userModel = documentSnapshot.toObject(User.class);
                        populateUserData(userModel);
                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    binding.progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load profile", Toast.LENGTH_SHORT).show();
                });
    }

    private void populateUserData(User user) {
        if (user != null) {
            binding.tvFullName.setText(user.getFullName());
            binding.tvEmail.setText(user.getEmail());
            binding.tvRole.setText(user.getRole());
            binding.tvLevel.setText(user.getLevel());
            binding.tvGroups.setText(user.getGroups());
            
            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.logo)
                        .into(binding.ivProfileImage);
            }
        }
    }
}
