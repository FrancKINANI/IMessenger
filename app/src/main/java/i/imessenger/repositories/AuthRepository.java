package i.imessenger.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import i.imessenger.models.User;

public class AuthRepository {

    private final FirebaseAuth mAuth;
    private final FirebaseFirestore db;

    public AuthRepository() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<FirebaseUser> getCurrentUser() {
        MutableLiveData<FirebaseUser> userLiveData = new MutableLiveData<>();
        userLiveData.setValue(mAuth.getCurrentUser());
        return userLiveData;
    }

    public LiveData<Boolean> login(String email, String password) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    public LiveData<Boolean> register(String email, String password, String name, String role, String level) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        User user = new User(
                                firebaseUser.getUid(),
                                email,
                                name,
                                role,
                                level,
                                "", // Profile Image
                                "", // FCM Token
                                ""  // Groups
                        );
                        db.collection("users").document(firebaseUser.getUid())
                                .set(user)
                                .addOnSuccessListener(unused -> result.setValue(true))
                                .addOnFailureListener(e -> result.setValue(false));
                    } else {
                        result.setValue(false);
                    }
                })
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    public void logout() {
        mAuth.signOut();
    }

    public LiveData<Boolean> loginWithGoogle(String idToken) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        com.google.firebase.auth.AuthCredential credential = com.google.firebase.auth.GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser != null) {
                        checkUserInFirestore(firebaseUser, result);
                    } else {
                        result.setValue(false);
                    }
                })
                .addOnFailureListener(e -> result.setValue(false));
        return result;
    }

    private void checkUserInFirestore(FirebaseUser firebaseUser, MutableLiveData<Boolean> result) {
        db.collection("users").document(firebaseUser.getUid()).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        result.setValue(true);
                    } else {
                        saveUserToFirestore(firebaseUser, result);
                    }
                })
                .addOnFailureListener(e -> result.setValue(false));
    }

    private void saveUserToFirestore(FirebaseUser firebaseUser, MutableLiveData<Boolean> result) {
        User user = new User(
                firebaseUser.getUid(),
                firebaseUser.getEmail(),
                firebaseUser.getDisplayName(),
                "student",
                "1st Year",
                firebaseUser.getPhotoUrl() != null ? firebaseUser.getPhotoUrl().toString() : "",
                "",
                ""
        );

        db.collection("users").document(firebaseUser.getUid())
                .set(user)
                .addOnSuccessListener(unused -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
    }
}
