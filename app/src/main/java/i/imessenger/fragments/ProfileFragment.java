package i.imessenger.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import i.imessenger.R;
import i.imessenger.databinding.FragmentProfileBinding;
import i.imessenger.models.User;
import i.imessenger.viewmodels.ProfileViewModel;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ProfileViewModel profileViewModel;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        loadUserProfile();

        binding.btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), i.imessenger.activities.SettingsActivity.class));
        });

        binding.ivProfileImage.setOnClickListener(v -> pickImage.launch("image/*"));
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            profileViewModel.getCurrentUser().observe(getViewLifecycleOwner(), user -> {
                if (user != null) {
                    populateUserData(user);
                } else {
                    // Fallback: Create user document if it doesn't exist
                    createMissingUserDocument(currentUser);
                }
            });
        }
    }

    private void createMissingUserDocument(FirebaseUser firebaseUser) {
        profileViewModel.createMissingUserDocument(firebaseUser).observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                populateUserData(user);
                Toast.makeText(getContext(), "Profile created", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Failed to create profile", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUserData(User user) {
        if (user != null) {
            binding.etFullName.setText(user.getFullName());
            binding.etEmail.setText(user.getEmail());
            binding.etRole.setText(user.getRole());
            binding.etLevel.setText(user.getLevel());
            binding.etGroups.setText(user.getGroups());

            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(getContext())
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.logo)
                        .into(binding.ivProfileImage);
            }
        }
    }

    private final androidx.activity.result.ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    binding.ivProfileImage.setImageURI(uri);
                    // Upload to Firebase Storage would go here
                    Toast.makeText(getContext(), "Image selected (Upload logic to be implemented)", Toast.LENGTH_SHORT).show();
                }
            }
    );
}
