package i.imessenger.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import java.util.Map;

import i.imessenger.models.User;
import i.imessenger.repositories.UserRepository;

public class ProfileViewModel extends ViewModel {

    private final UserRepository userRepository;

    public ProfileViewModel() {
        userRepository = UserRepository.getInstance();
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public LiveData<User> getUser(String userId) {
        return userRepository.getUser(userId);
    }

    public LiveData<Boolean> updateUserProfile(String userId, Map<String, Object> updates) {
        return userRepository.updateUserProfile(userId, updates);
    }

    public LiveData<User> createMissingUserDocument(com.google.firebase.auth.FirebaseUser firebaseUser) {
        return userRepository.createMissingUserDocument(firebaseUser);
    }
}
