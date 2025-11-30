package I.imessenger.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import I.imessenger.models.User;

public class UserRepository {

    private final FirebaseFirestore db;
    private final String currentUserId;

    public UserRepository() {
        db = FirebaseFirestore.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public LiveData<User> getUser(String userId) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userLiveData.setValue(documentSnapshot.toObject(User.class));
                    } else {
                        userLiveData.setValue(null);
                    }
                })
                .addOnFailureListener(e -> userLiveData.setValue(null));
        return userLiveData;
    }

    public LiveData<User> getCurrentUser() {
        if (currentUserId == null) return new MutableLiveData<>(null);
        return getUser(currentUserId);
    }

    public LiveData<Boolean> updateUserProfile(String userId, Map<String, Object> updates) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(unused -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    public LiveData<User> createMissingUserDocument(com.google.firebase.auth.FirebaseUser firebaseUser) {
        MutableLiveData<User> userLiveData = new MutableLiveData<>();
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
                .addOnSuccessListener(aVoid -> userLiveData.setValue(user))
                .addOnFailureListener(e -> userLiveData.setValue(null));
        return userLiveData;
    }
}
