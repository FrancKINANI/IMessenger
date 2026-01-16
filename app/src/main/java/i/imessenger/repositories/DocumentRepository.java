package i.imessenger.repositories;

import android.net.Uri;
import android.util.Log;

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
import java.util.List;
import java.util.UUID;

import i.imessenger.models.DocumentFile;

public class DocumentRepository {

    private static final String TAG = "DocumentRepository";
    private final FirebaseFirestore db;
    private final FirebaseStorage storage;
    private final String currentUserId;

    private static volatile DocumentRepository instance;

    private DocumentRepository() {
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        currentUserId = FirebaseAuth.getInstance().getUid();
    }

    public static DocumentRepository getInstance() {
        if (instance == null) {
            synchronized (DocumentRepository.class) {
                if (instance == null) {
                    instance = new DocumentRepository();
                }
            }
        }
        return instance;
    }

    public LiveData<List<DocumentFile>> getDocuments(String folder, String userLevel) {
        MutableLiveData<List<DocumentFile>> documentsLiveData = new MutableLiveData<>();

        Query query = db.collection("documents");

        if (folder != null && !folder.isEmpty()) {
            query = query.whereEqualTo("folder", folder);
        }

        // Optionally filter by level here or in UI
        if (userLevel != null && !userLevel.isEmpty()) {
            // For simplicity, we might fetch all and filter in memory,
            // but let's assume we want public docs OR docs for this level.
            // Firestore limitations on OR queries might make this tricky.
            // For MVP: Fetch all in folder and sort by date.
        }

        query.orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        Log.e(TAG, "Error fetching documents", error);
                        documentsLiveData.setValue(new ArrayList<>());
                        return;
                    }

                    List<DocumentFile> files = new ArrayList<>();
                    if (value != null) {
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            DocumentFile file = doc.toObject(DocumentFile.class);
                            if (file != null) {
                                file.setId(doc.getId());
                                files.add(file);
                            }
                        }
                    }
                    documentsLiveData.setValue(files);
                });

        return documentsLiveData;
    }

    public LiveData<Boolean> uploadDocument(String name, Uri fileUri, String type, String folder, String targetClass,
            String uploaderName) {
        MutableLiveData<Boolean> result = new MutableLiveData<>();

        String filename = UUID.randomUUID().toString();
        StorageReference ref = storage.getReference().child("documents/" + folder + "/" + filename);

        ref.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> {
                    ref.getDownloadUrl().addOnSuccessListener(uri -> {
                        String downloadUrl = uri.toString();
                        long size = taskSnapshot.getTotalByteCount();

                        saveDocumentToFirestore(name, downloadUrl, type, size, folder, targetClass, uploaderName,
                                result);
                    });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Upload failed", e);
                    result.setValue(false);
                });

        return result;
    }

    private void saveDocumentToFirestore(String name, String url, String type, long size, String folder,
            String targetClass, String uploaderName, MutableLiveData<Boolean> result) {
        String docId = db.collection("documents").document().getId();

        DocumentFile file = new DocumentFile(
                docId,
                name,
                url,
                type,
                size,
                currentUserId,
                uploaderName,
                Timestamp.now(),
                folder,
                targetClass);

        db.collection("documents").document(docId)
                .set(file)
                .addOnSuccessListener(aVoid -> result.setValue(true))
                .addOnFailureListener(e -> result.setValue(false));
    }

    public void deleteDocument(DocumentFile file) {
        // Delete from Firestore
        db.collection("documents").document(file.getId()).delete();

        // Attempt delete from Storage (might fail if we don't store path perfectly, but
        // we constructed it differently)
        // Ideally we should store storagePath in model.
        // For MVP, we just delete the link.
    }
}
