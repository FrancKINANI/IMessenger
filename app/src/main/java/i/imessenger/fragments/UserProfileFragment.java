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

import i.imessenger.R;
import i.imessenger.databinding.FragmentUserProfileBinding;
import i.imessenger.models.User;
import i.imessenger.viewmodels.ProfileViewModel;

public class UserProfileFragment extends Fragment {

    private FragmentUserProfileBinding binding;
    private ProfileViewModel profileViewModel;
    private User userModel;
    private String userId;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        binding = FragmentUserProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
        }

        if (userId == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding.toolbar.setNavigationOnClickListener(v -> NavHostFragment.findNavController(this).navigateUp());

        loadUserProfile();

        binding.btnMessage.setOnClickListener(v -> {
            if (userModel != null) {
                Bundle args = new Bundle();
                args.putString("userId", userModel.getUid());
                args.putString("userName", userModel.getFullName());
                args.putString("userImage", userModel.getProfileImage());
                NavHostFragment.findNavController(this).navigate(R.id.action_userProfileFragment_to_chatFragment, args);
            }
        });
    }

    private void loadUserProfile() {
        binding.progressBar.setVisibility(View.VISIBLE);
        profileViewModel.getUser(userId).observe(getViewLifecycleOwner(), user -> {
            binding.progressBar.setVisibility(View.GONE);
            if (user != null) {
                userModel = user;
                populateUserData(user);
            } else {
                Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                NavHostFragment.findNavController(this).navigateUp();
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

            if (user.getSkills() != null && !user.getSkills().isEmpty()) {
                binding.tvSkills.setText(android.text.TextUtils.join(", ", user.getSkills()));
                ((View) binding.tvSkills.getParent()).setVisibility(View.VISIBLE);
            } else {
                ((View) binding.tvSkills.getParent()).setVisibility(View.GONE);
            }

            if (user.getLinkedin() != null && !user.getLinkedin().isEmpty()) {
                binding.tvLinkedin.setText(user.getLinkedin());
                ((View) binding.tvLinkedin.getParent()).setVisibility(View.VISIBLE);
            } else {
                ((View) binding.tvLinkedin.getParent()).setVisibility(View.GONE);
            }

            if (user.getPortfolio() != null && !user.getPortfolio().isEmpty()) {
                binding.tvPortfolio.setText(user.getPortfolio());
                ((View) binding.tvPortfolio.getParent()).setVisibility(View.VISIBLE);
            } else {
                ((View) binding.tvPortfolio.getParent()).setVisibility(View.GONE);
            }

            if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
                Glide.with(this)
                        .load(user.getProfileImage())
                        .placeholder(R.drawable.logo)
                        .into(binding.ivProfileImage);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
