package I.imessenger.viewmodels;

import androidx.lifecycle.ViewModel;

import I.imessenger.repositories.AuthRepository;

public class SettingsViewModel extends ViewModel {

    private final AuthRepository authRepository;

    public SettingsViewModel() {
        authRepository = new AuthRepository();
    }

    public void logout() {
        authRepository.logout();
    }
}
