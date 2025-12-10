package i.imessenger.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;

import i.imessenger.R;
import i.imessenger.databinding.ActivityUserProfileBinding;
import i.imessenger.models.User;
import i.imessenger.viewmodels.ProfileViewModel;

public class UserProfileActivity extends AppCompatActivity {

    private ActivityUserProfileBinding binding;
    private ProfileViewModel profileViewModel;
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

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());

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
        profileViewModel.getUser(userId).observe(this, user -> {
            binding.progressBar.setVisibility(View.GONE);
            if (user != null) {
                userModel = user;
                populateUserData(user);
            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                finish();
            }
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
