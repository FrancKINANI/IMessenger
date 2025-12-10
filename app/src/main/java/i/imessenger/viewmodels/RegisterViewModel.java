package i.imessenger.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import i.imessenger.repositories.AuthRepository;

public class RegisterViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public RegisterViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Boolean> register(String email, String password, String name, String role, String level) {
        return authRepository.register(email, password, name, role, level);
    }

    public LiveData<Boolean> loginWithGoogle(String idToken) {
        return authRepository.loginWithGoogle(idToken);
    }
}
