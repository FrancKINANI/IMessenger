package i.imessenger.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import i.imessenger.models.Comment;
import i.imessenger.models.FeedPost;
import i.imessenger.models.User;
import i.imessenger.repositories.FeedRepository;
import i.imessenger.repositories.UserRepository;

public class FeedViewModel extends ViewModel {

    private final FeedRepository feedRepository;
    private final UserRepository userRepository;
    private MutableLiveData<User> currentUser = new MutableLiveData<>();
    private MutableLiveData<Boolean> isRefreshing = new MutableLiveData<>(false);

    public FeedViewModel() {
        feedRepository = FeedRepository.getInstance();
        userRepository = UserRepository.getInstance();
        loadCurrentUser();
    }

    private void loadCurrentUser() {
        userRepository.getCurrentUser().observeForever(user -> {
            currentUser.setValue(user);
        });
    }

    public LiveData<User> getCurrentUser() {
        return currentUser;
    }

    public LiveData<List<FeedPost>> getFeedPosts() {
        return feedRepository.getFeedPosts();
    }

    public LiveData<List<FeedPost>> getUserPosts(String userId) {
        return feedRepository.getUserPosts(userId);
    }

    public LiveData<Boolean> createPost(String content, List<Uri> mediaUris, List<String> mediaTypes,
            List<String> mediaNames, String visibility, String targetClass) {
        User user = currentUser.getValue();
        if (user == null) {
            MutableLiveData<Boolean> result = new MutableLiveData<>();
            result.setValue(false);
            return result;
        }
        return feedRepository.createPost(content, mediaUris, mediaTypes, mediaNames, visibility, targetClass, user);
    }

    public void toggleLike(String postId) {
        String userId = feedRepository.getCurrentUserId();
        if (userId != null) {
            feedRepository.toggleLike(postId, userId);
        }
    }

    public LiveData<List<Comment>> getComments(String postId) {
        return feedRepository.getComments(postId);
    }

    public LiveData<Boolean> addComment(String postId, String content) {
        User user = currentUser.getValue();
        if (user == null) {
            MutableLiveData<Boolean> result = new MutableLiveData<>();
            result.setValue(false);
            return result;
        }
        return feedRepository.addComment(postId, content, user);
    }

    public LiveData<Boolean> deletePost(String postId) {
        return feedRepository.deletePost(postId);
    }

    public LiveData<Boolean> reportPost(String postId, String reason) {
        return feedRepository.reportPost(postId, reason);
    }

    public void incrementViewCount(String postId) {
        feedRepository.incrementViewCount(postId);
    }

    public boolean isCurrentUser(String userId) {
        return feedRepository.getCurrentUserId() != null &&
                feedRepository.getCurrentUserId().equals(userId);
    }

    public String getCurrentUserId() {
        return feedRepository.getCurrentUserId();
    }

    public LiveData<Boolean> isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean refreshing) {
        isRefreshing.setValue(refreshing);
    }
}
