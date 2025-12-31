package i.imessenger.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import i.imessenger.models.MediaItem;
import i.imessenger.repositories.MediaRepository;

public class MediaViewModel extends ViewModel {

    private final MediaRepository mediaRepository;
    private MutableLiveData<Integer> uploadProgress = new MutableLiveData<>(0);
    private MutableLiveData<Boolean> isUploading = new MutableLiveData<>(false);
    private MutableLiveData<String> uploadError = new MutableLiveData<>();

    public MediaViewModel() {
        mediaRepository = new MediaRepository();
    }

    public LiveData<List<MediaItem>> getUserMedia(String userId) {
        return mediaRepository.getUserMedia(userId);
    }

    public LiveData<List<MediaItem>> getUserImages(String userId) {
        return mediaRepository.getUserImages(userId);
    }

    public LiveData<List<MediaItem>> getUserVideos(String userId) {
        return mediaRepository.getUserVideos(userId);
    }

    public void uploadImage(Uri imageUri, String caption, UploadCompleteCallback callback) {
        isUploading.setValue(true);
        uploadProgress.setValue(0);

        mediaRepository.uploadImage(imageUri, caption, new MediaRepository.UploadCallback() {
            @Override
            public void onProgress(int progress) {
                uploadProgress.postValue(progress);
            }

            @Override
            public void onSuccess(MediaItem mediaItem) {
                isUploading.postValue(false);
                uploadProgress.postValue(100);
                callback.onComplete(true, mediaItem);
            }

            @Override
            public void onFailure(String error) {
                isUploading.postValue(false);
                uploadError.postValue(error);
                callback.onComplete(false, null);
            }
        });
    }

    public void uploadVideo(Uri videoUri, String caption, UploadCompleteCallback callback) {
        isUploading.setValue(true);
        uploadProgress.setValue(0);

        mediaRepository.uploadVideo(videoUri, caption, new MediaRepository.UploadCallback() {
            @Override
            public void onProgress(int progress) {
                uploadProgress.postValue(progress);
            }

            @Override
            public void onSuccess(MediaItem mediaItem) {
                isUploading.postValue(false);
                uploadProgress.postValue(100);
                callback.onComplete(true, mediaItem);
            }

            @Override
            public void onFailure(String error) {
                isUploading.postValue(false);
                uploadError.postValue(error);
                callback.onComplete(false, null);
            }
        });
    }

    public LiveData<Boolean> deleteMedia(String mediaId, String url) {
        return mediaRepository.deleteMedia(mediaId, url);
    }

    public LiveData<Boolean> updateProfileImage(Uri imageUri) {
        return mediaRepository.updateProfileImage(imageUri);
    }

    public LiveData<Integer> getUploadProgress() {
        return uploadProgress;
    }

    public LiveData<Boolean> isUploading() {
        return isUploading;
    }

    public LiveData<String> getUploadError() {
        return uploadError;
    }

    public String getCurrentUserId() {
        return mediaRepository.getCurrentUserId();
    }

    public interface UploadCompleteCallback {
        void onComplete(boolean success, MediaItem mediaItem);
    }
}

