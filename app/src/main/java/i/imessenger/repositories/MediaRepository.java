package i.imessenger.repositories;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import i.imessenger.models.MediaItem;

public class MediaRepository {

    private static final String TAG = "MediaRepository";
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final String currentUserId;

    // Cache for user media LiveData to avoid multiple listeners
    private final Map<String, MutableLiveData<List<MediaItem>>> userMediaCache = new HashMap<>();

    public MediaRepository() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public LiveData<List<MediaItem>> getUserMedia(String userId) {
        // Return cached LiveData if available
        if (userMediaCache.containsKey(userId)) {
            return userMediaCache.get(userId);
        }

        MutableLiveData<List<MediaItem>> mediaLiveData = new MutableLiveData<>();
        userMediaCache.put(userId, mediaLiveData);

        db.collection("media")
                .whereEqualTo("ownerId", userId)
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        android.util.Log.e(TAG, "Error fetching user media: " + error.getMessage(), error);
                        // Don't set empty list on error - keep previous value
                        if (mediaLiveData.getValue() == null) {
                            mediaLiveData.setValue(new ArrayList<>());
                        }
                        return;
                    }
                    if (value != null) {
                        List<MediaItem> mediaItems = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            MediaItem item = doc.toObject(MediaItem.class);
                            if (item != null) {
                                item.setMediaId(doc.getId());
                                mediaItems.add(item);
                            }
                        }
                        mediaLiveData.setValue(mediaItems);
                    }
                });
        return mediaLiveData;
    }

    public LiveData<List<MediaItem>> getUserImages(String userId) {
        MutableLiveData<List<MediaItem>> mediaLiveData = new MutableLiveData<>();

        db.collection("media")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("type", "image")
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        mediaLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    if (value != null) {
                        List<MediaItem> mediaItems = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            MediaItem item = doc.toObject(MediaItem.class);
                            if (item != null) {
                                item.setMediaId(doc.getId());
                                mediaItems.add(item);
                            }
                        }
                        mediaLiveData.setValue(mediaItems);
                    }
                });
        return mediaLiveData;
    }

    public LiveData<List<MediaItem>> getUserVideos(String userId) {
        MutableLiveData<List<MediaItem>> mediaLiveData = new MutableLiveData<>();

        db.collection("media")
                .whereEqualTo("ownerId", userId)
                .whereEqualTo("type", "video")
                .orderBy("uploadedAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        mediaLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    if (value != null) {
                        List<MediaItem> mediaItems = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            MediaItem item = doc.toObject(MediaItem.class);
                            if (item != null) {
                                item.setMediaId(doc.getId());
                                mediaItems.add(item);
                            }
                        }
                        mediaLiveData.setValue(mediaItems);
                    }
                });
        return mediaLiveData;
    }

    public interface UploadCallback {
        void onProgress(int progress);
        void onSuccess(MediaItem mediaItem);
        void onFailure(String error);
    }

    public void uploadImage(Uri imageUri, String caption, UploadCallback callback) {
        uploadMedia(imageUri, "image", caption, callback);
    }

    public void uploadVideo(Uri videoUri, String caption, UploadCallback callback) {
        uploadMedia(videoUri, "video", caption, callback);
    }

    private void uploadMedia(Uri mediaUri, String type, String caption, UploadCallback callback) {
        String mediaId = UUID.randomUUID().toString();
        String extension = type.equals("video") ? ".mp4" : ".jpg";
        String path = "media/" + currentUserId + "/" + type + "s/" + mediaId + extension;

        StorageReference ref = storage.getReference().child(path);

        ref.putFile(mediaUri)
                .addOnProgressListener(snapshot -> {
                    int progress = (int) ((100.0 * snapshot.getBytesTransferred()) / snapshot.getTotalByteCount());
                    callback.onProgress(progress);
                })
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        MediaItem mediaItem = new MediaItem(
                                mediaId,
                                currentUserId,
                                downloadUri.toString(),
                                downloadUri.toString(), // Thumbnail same as URL for images
                                type,
                                caption,
                                Timestamp.now(),
                                taskSnapshot.getTotalByteCount(),
                                0, 0, 0
                        );

                        saveMediaItem(mediaItem, callback);
                    }).addOnFailureListener(e -> callback.onFailure(e.getMessage()));
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    private void saveMediaItem(MediaItem mediaItem, UploadCallback callback) {
        db.collection("media").document(mediaItem.getMediaId())
                .set(mediaItem)
                .addOnSuccessListener(aVoid -> callback.onSuccess(mediaItem))
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public LiveData<Boolean> deleteMedia(String mediaId, String url) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        // Delete from Firestore
        db.collection("media").document(mediaId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Try to delete from Storage
                    try {
                        StorageReference ref = storage.getReferenceFromUrl(url);
                        ref.delete().addOnCompleteListener(task -> result.setValue(true));
                    } catch (Exception e) {
                        result.setValue(true); // Still success if Firestore deletion worked
                    }
                })
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    public LiveData<Boolean> updateProfileImage(Uri imageUri) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        String path = "profiles/" + currentUserId + "/profile_image.jpg";
        StorageReference ref = storage.getReference().child(path);

        ref.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("profileImage", downloadUri.toString());

                        db.collection("users").document(currentUserId)
                                .update(updates)
                                .addOnSuccessListener(aVoid -> result.setValue(true))
                                .addOnFailureListener(e -> result.setValue(false));
                    }).addOnFailureListener(e -> result.setValue(false));
                })
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    public String getCurrentUserId() {
        return currentUserId;
    }
}

