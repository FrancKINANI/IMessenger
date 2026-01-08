package i.imessenger.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseUser;

import i.imessenger.repositories.AuthRepository;

public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public LoginViewModel() {
        authRepository = AuthRepository.getInstance();
    }

    public LiveData<Boolean> login(String email, String password) {
        return authRepository.login(email, password);
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        return authRepository.getCurrentUser();
    }

    public LiveData<Boolean> loginWithGoogle(String idToken) {
        return authRepository.loginWithGoogle(idToken);
    }
}
