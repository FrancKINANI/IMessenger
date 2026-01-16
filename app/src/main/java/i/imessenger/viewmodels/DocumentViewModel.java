package i.imessenger.viewmodels;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import i.imessenger.models.DocumentFile;
import i.imessenger.models.User;
import i.imessenger.repositories.DocumentRepository;
import i.imessenger.repositories.UserRepository;

public class DocumentViewModel extends ViewModel {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final MutableLiveData<String> currentFolder = new MutableLiveData<>("General");

    public DocumentViewModel() {
        documentRepository = DocumentRepository.getInstance();
        userRepository = UserRepository.getInstance();
    }

    public LiveData<List<DocumentFile>> getDocuments(String userLevel) {
        // We observe the current folder and fetch documents for it
        // A better approach might be to expose a method that returns LiveData based on
        // folder
        // But to keep it reactive, usually we switchMap.
        // For simplicity:
        return documentRepository.getDocuments(currentFolder.getValue(), userLevel);
    }

    public void setFolder(String folder) {
        currentFolder.setValue(folder);
    }

    public LiveData<String> getCurrentFolder() {
        return currentFolder;
    }

    public LiveData<Boolean> uploadDocument(String name, Uri fileUri, String type, String targetClass) {
        User user = userRepository.getCurrentUser().getValue();
        // Fallback if not loaded yet, though unlikely in this flow
        String uploaderName = (user != null) ? user.getFullName() : "Unknown";

        return documentRepository.uploadDocument(name, fileUri, type, currentFolder.getValue(), targetClass,
                uploaderName);
    }

    public LiveData<User> getCurrentUser() {
        return userRepository.getCurrentUser();
    }

    public void deleteDocument(DocumentFile file) {
        documentRepository.deleteDocument(file);
    }
}
