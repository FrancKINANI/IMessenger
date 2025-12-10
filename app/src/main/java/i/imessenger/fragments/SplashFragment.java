package i.imessenger.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.NavOptions;

import com.google.firebase.auth.FirebaseAuth;

import i.imessenger.R;
import i.imessenger.databinding.FragmentSplashBinding;

public class SplashFragment extends Fragment {

    private FragmentSplashBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSplashBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            NavController navController = Navigation.findNavController(view);
            
            // Remove SplashFragment from back stack so user can't go back to it
            NavOptions navOptions = new NavOptions.Builder()
                    .setPopUpTo(R.id.splashFragment, true)
                    .build();

            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                navController.navigate(R.id.action_splashFragment_to_homeFragment, null, navOptions);
            } else {
                navController.navigate(R.id.action_splashFragment_to_loginFragment, null, navOptions);
            }
        }, 2000); // 2 seconds delay
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
