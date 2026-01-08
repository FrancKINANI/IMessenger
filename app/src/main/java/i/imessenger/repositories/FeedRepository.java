package i.imessenger.repositories;

import android.net.Uri;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import i.imessenger.models.Comment;
import i.imessenger.models.FeedPost;
import i.imessenger.models.User;

public class FeedRepository {

    private static final String TAG = "FeedRepository";
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final String currentUserId;

    // Cache for user posts LiveData to avoid multiple listeners
    private final java.util.Map<String, MutableLiveData<List<FeedPost>>> userPostsCache = new java.util.HashMap<>();
    private MutableLiveData<List<FeedPost>> cachedFeedPosts;

    private static volatile FeedRepository instance;

    private FeedRepository() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public static FeedRepository getInstance() {
        if (instance == null) {
            synchronized (FeedRepository.class) {
                if (instance == null) {
                    instance = new FeedRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<FeedPost>> getFeedPosts() {
        if (cachedFeedPosts != null) {
            return cachedFeedPosts;
        }
        cachedFeedPosts = new MutableLiveData<>();

        db.collection("posts")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(50)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        cachedFeedPosts.setValue(new ArrayList<>());
                        return;
                    }
                    if (value != null) {
                        List<FeedPost> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            FeedPost post = doc.toObject(FeedPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                posts.add(post);
                            }
                        }
                        cachedFeedPosts.setValue(posts);
                    }
                });
        return cachedFeedPosts;
    }

    public LiveData<List<FeedPost>> getUserPosts(String userId) {
        // Return cached LiveData if available
        if (userPostsCache.containsKey(userId)) {
            return userPostsCache.get(userId);
        }

        MutableLiveData<List<FeedPost>> postsLiveData = new MutableLiveData<>();
        userPostsCache.put(userId, postsLiveData);

        db.collection("posts")
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching user posts: " + error.getMessage(), error);
                        // Don't set empty list on error - keep previous value or set null
                        if (postsLiveData.getValue() == null) {
                            postsLiveData.setValue(new ArrayList<>());
                        }
                        return;
                    }
                    if (value != null) {
                        List<FeedPost> posts = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            FeedPost post = doc.toObject(FeedPost.class);
                            if (post != null) {
                                post.setPostId(doc.getId());
                                posts.add(post);
                            }
                        }
                        postsLiveData.setValue(posts);
                    }
                });
        return postsLiveData;
    }

    public LiveData<Boolean> createPost(String content, List<Uri> mediaUris, List<String> mediaTypes,
            List<String> mediaNames, String visibility, String targetClass, User author) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        if (mediaUris != null && !mediaUris.isEmpty()) {
            uploadMediaAndCreatePost(content, mediaUris, mediaTypes, mediaNames, visibility, targetClass, author,
                    result);
        } else {
            createPostDocument(content, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), visibility,
                    targetClass, author, result);
        }

        return result;
    }

    private void uploadMediaAndCreatePost(String content, List<Uri> mediaUris, List<String> mediaTypes,
            List<String> mediaNames, String visibility, String targetClass, User author,
            MutableLiveData<Boolean> result) {
        List<String> uploadedUrls = new ArrayList<>();
        uploadMediaRecursively(mediaUris, mediaTypes, 0, uploadedUrls, () -> {
            createPostDocument(content, uploadedUrls, mediaTypes, mediaNames, visibility, targetClass, author, result);
        }, () -> result.setValue(false));
    }

    private void uploadMediaRecursively(List<Uri> mediaUris, List<String> mediaTypes, int index,
            List<String> uploadedUrls, Runnable onSuccess, Runnable onFailure) {
        if (index >= mediaUris.size()) {
            onSuccess.run();
            return;
        }

        Uri uri = mediaUris.get(index);
        if (uri == null) {
            Log.e(TAG, "Media URI at index " + index + " is null");
            onFailure.run();
            return;
        }

        // Safe access to mediaTypes - default to "image" if index is out of bounds
        String type = (mediaTypes != null && index < mediaTypes.size()) ? mediaTypes.get(index) : "image";
        String fileName = UUID.randomUUID().toString();
        String extension;
        String folder;

        if ("video".equals(type)) {
            extension = ".mp4";
            folder = "posts/videos/";
        } else if ("document".equals(type)) {
            extension = ""; // Keep original extension from URI
            folder = "posts/documents/";
        } else {
            extension = ".jpg";
            folder = "posts/images/";
        }

        String path = folder + fileName + extension;

        Log.d(TAG, "Uploading media: " + uri.toString() + " to path: " + path);

        StorageReference ref = storage.getReference().child(path);
        ref.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> {
                    Log.d(TAG, "Upload successful for: " + path);
                    ref.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                        uploadedUrls.add(downloadUri.toString());
                        uploadMediaRecursively(mediaUris, mediaTypes, index + 1, uploadedUrls, onSuccess, onFailure);
                    }).addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get download URL: " + e.getMessage(), e);
                        onFailure.run();
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to upload media at index " + index + ": " + e.getMessage(), e);
                    onFailure.run();
                });
    }

    private void createPostDocument(String content, List<String> mediaUrls, List<String> mediaTypes,
            List<String> mediaNames, String visibility, String targetClass, User author,
            MutableLiveData<Boolean> result) {
        String postId = db.collection("posts").document().getId();

        FeedPost post = new FeedPost(
                postId,
                currentUserId,
                author.getFullName(),
                author.getProfileImage(),
                author.getRole(),
                content,
                mediaUrls,
                mediaTypes,
                Timestamp.now(),
                visibility,
                targetClass);
        post.setMediaNames(mediaNames);

        Log.d(TAG, "Creating post with " + mediaUrls.size() + " media items");

        db.collection("posts").document(postId)
                .set(post)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Post created successfully with ID: " + postId);
                    result.setValue(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to create post: " + e.getMessage(), e);
                    result.setValue(false);
                });
    }

    public void toggleLike(String postId, String userId) {
        db.collection("posts").document(postId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    FeedPost post = documentSnapshot.toObject(FeedPost.class);
                    if (post != null) {
                        List<String> likedBy = post.getLikedBy();
                        if (likedBy == null)
                            likedBy = new ArrayList<>();

                        if (likedBy.contains(userId)) {
                            db.collection("posts").document(postId)
                                    .update("likedBy", FieldValue.arrayRemove(userId));
                        } else {
                            db.collection("posts").document(postId)
                                    .update("likedBy", FieldValue.arrayUnion(userId));
                        }
                    }
                });
    }

    public LiveData<List<Comment>> getComments(String postId) {
        MutableLiveData<List<Comment>> commentsLiveData = new MutableLiveData<>();

        db.collection("posts").document(postId).collection("comments")
                .orderBy("createdAt", Query.Direction.ASCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        commentsLiveData.setValue(new ArrayList<>());
                        return;
                    }
                    if (value != null) {
                        List<Comment> comments = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            Comment comment = doc.toObject(Comment.class);
                            if (comment != null) {
                                comment.setCommentId(doc.getId());
                                comments.add(comment);
                            }
                        }
                        commentsLiveData.setValue(comments);
                    }
                });
        return commentsLiveData;
    }

    public LiveData<Boolean> addComment(String postId, String content, User author) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        String commentId = db.collection("posts").document(postId)
                .collection("comments").document().getId();

        Comment comment = new Comment(
                commentId,
                postId,
                currentUserId,
                author.getFullName(),
                author.getProfileImage(),
                content,
                Timestamp.now());

        db.collection("posts").document(postId).collection("comments")
                .document(commentId)
                .set(comment)
                .addOnSuccessListener(aVoid -> {
                    // Update comment count
                    db.collection("posts").document(postId)
                            .update("commentCount", FieldValue.increment(1));
                    result.setValue(true);
                })
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    public LiveData<Boolean> deletePost(String postId) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        db.collection("posts").document(postId)
                .delete()
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));

        return result;
    }

    public LiveData<Boolean> reportPost(String postId, String reason) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();
        String reportId = db.collection("reports").document().getId();

        i.imessenger.models.Report report = new i.imessenger.models.Report(
                reportId,
                currentUserId,
                postId,
                "POST",
                reason,
                Timestamp.now());

        db.collection("reports").document(reportId)
                .set(report)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Report failed", e);
                    result.setValue(false);
                });

        return result;
    }

    public void incrementViewCount(String postId) {
        db.collection("posts").document(postId)
                .update("viewCount", FieldValue.increment(1));
    }

    public String getCurrentUserId() {
        return currentUserId;
    }
}
