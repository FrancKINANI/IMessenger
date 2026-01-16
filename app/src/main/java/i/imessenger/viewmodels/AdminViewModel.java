package i.imessenger.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.models.Report;

public class AdminViewModel extends ViewModel {

    private final FirebaseFirestore db;

    public AdminViewModel() {
        db = FirebaseFirestore.getInstance();
    }

    public LiveData<List<Report>> getReports() {
        MutableLiveData<List<Report>> reports = new MutableLiveData<>();

        db.collection("reports")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (value != null) {
                        List<Report> list = new ArrayList<>();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            list.add(doc.toObject(Report.class));
                        }
                        reports.setValue(list);
                    }
                });

        return reports;
    }

    public void resolveReport(String reportId) {
        db.collection("reports").document(reportId).update("status", "RESOLVED");
    }

    public void deleteTarget(String collection, String docId) {
        db.collection(collection).document(docId).delete();
    }

    public LiveData<Long> getUserCount() {
        MutableLiveData<Long> count = new MutableLiveData<>();
        db.collection("users").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                count.setValue((long) task.getResult().size());
            } else {
                count.setValue(0L);
            }
        });
        return count;
    }

    public LiveData<Long> getPostCount() {
        MutableLiveData<Long> count = new MutableLiveData<>();
        db.collection("posts").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                count.setValue((long) task.getResult().size());
            } else {
                count.setValue(0L);
            }
        });
        return count;
    }

    public LiveData<Long> getReportCount() {
        MutableLiveData<Long> count = new MutableLiveData<>();
        db.collection("reports").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                count.setValue((long) task.getResult().size());
            } else {
                count.setValue(0L);
            }
        });
        return count;
    }
}
