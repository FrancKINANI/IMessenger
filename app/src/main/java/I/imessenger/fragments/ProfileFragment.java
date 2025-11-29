package I.imessenger.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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
import I.imessenger.activities.LoginActivity;
import I.imessenger.databinding.FragmentProfileBinding;
import I.imessenger.models.User;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private User currentUserModel;

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

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        loadUserProfile();

        binding.btnSettings.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), I.imessenger.activities.SettingsActivity.class));
        });

        binding.ivProfileImage.setOnClickListener(v -> pickImage.launch("image/*"));
    }

    private void loadUserProfile() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid()).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                currentUserModel = documentSnapshot.toObject(User.class);
                                populateUserData(currentUserModel);
                            } else {
                                // Fallback: Create user document if it doesn't exist
                                createMissingUserDocument(currentUser);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            // Even if Firestore fails, show what we have from Auth
                            binding.etEmail.setText(currentUser.getEmail());
                            binding.etFullName.setText(currentUser.getDisplayName());
                            Toast.makeText(getContext(), "Failed to load profile: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void createMissingUserDocument(FirebaseUser firebaseUser) {
        User user = new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName() != null ? firebaseUser.getDisplayName() : "New User",
                "Student", // Default
                "1st Year", // Default
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "",
                "",
                ""
        );

        db.collection("users").document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    currentUserModel = user;
                    populateUserData(user);
                    Toast.makeText(getContext(), "Profile created", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to create profile", Toast.LENGTH_SHORT).show();
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

    private void enableEditing(boolean enable) {
        // No longer needed
    }

    private void saveUserProfile() {
        // No longer needed
    }

    private final androidx.activity.result.ActivityResultLauncher<String> pickImage = registerForActivityResult(
            new androidx.activity.result.contract.ActivityResultContracts.GetContent(),
            uri -> {
                if (uri != null) {
                    binding.ivProfileImage.setImageURI(uri);
                    // Upload to Firebase Storage would go here
                    // For now, we just show it locally as a placeholder for the "upload" logic
                    Toast.makeText(getContext(), "Image selected (Upload logic to be implemented)", Toast.LENGTH_SHORT).show();
                }
            }
    );
}
