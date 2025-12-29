package i.imessenger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import i.imessenger.R;
import i.imessenger.adapters.PollAdapter;
import i.imessenger.databinding.FragmentPollsBinding;
import i.imessenger.models.Poll;
import i.imessenger.models.User;

public class PollsFragment extends Fragment implements PollAdapter.OnPollVoteListener {

    private FragmentPollsBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private PollAdapter adapter;

    private String currentUserId;
    private String userName = "";

    private int visibleOptionsCount = 2;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPollsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser() != null ? auth.getCurrentUser().getUid() : "";

        setupToolbar();
        setupRecyclerView();
        loadUserInfo();

        binding.fabAddPoll.setOnClickListener(v -> showAddPollDialog());
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                Navigation.findNavController(v).navigateUp()
        );
    }

    private void setupRecyclerView() {
        adapter = new PollAdapter(currentUserId, this);
        binding.recyclerPolls.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerPolls.setAdapter(adapter);
    }

    private void loadUserInfo() {
        if (currentUserId.isEmpty()) return;

        db.collection("users").document(currentUserId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            userName = user.getFullName() != null ? user.getFullName() : "";
                        }
                    }
                    loadPolls();
                });
    }

    private void loadPolls() {
        db.collection("polls")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Poll> polls = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Poll poll = doc.toObject(Poll.class);
                        polls.add(poll);
                    }

                    adapter.setPolls(polls);
                    binding.textNoPolls.setVisibility(polls.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }

    private void showAddPollDialog() {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_poll, null);

        TextInputEditText editQuestion = dialogView.findViewById(R.id.editQuestion);
        TextInputEditText editOption1 = dialogView.findViewById(R.id.editOption1);
        TextInputEditText editOption2 = dialogView.findViewById(R.id.editOption2);
        TextInputEditText editOption3 = dialogView.findViewById(R.id.editOption3);
        TextInputEditText editOption4 = dialogView.findViewById(R.id.editOption4);
        TextInputLayout layoutOption3 = dialogView.findViewById(R.id.layoutOption3);
        TextInputLayout layoutOption4 = dialogView.findViewById(R.id.layoutOption4);
        SwitchMaterial switchAnonymous = dialogView.findViewById(R.id.switchAnonymous);
        SwitchMaterial switchMultipleVotes = dialogView.findViewById(R.id.switchMultipleVotes);
        View btnAddOption = dialogView.findViewById(R.id.btnAddOption);

        visibleOptionsCount = 2;

        btnAddOption.setOnClickListener(v -> {
            if (visibleOptionsCount == 2) {
                layoutOption3.setVisibility(View.VISIBLE);
                visibleOptionsCount = 3;
            } else if (visibleOptionsCount == 3) {
                layoutOption4.setVisibility(View.VISIBLE);
                visibleOptionsCount = 4;
                btnAddOption.setVisibility(View.GONE);
            }
        });

        AlertDialog dialog = new MaterialAlertDialogBuilder(getContext())
                .setView(dialogView)
                .create();

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnCreate).setOnClickListener(v -> {
            String question = editQuestion.getText().toString().trim();
            String option1 = editOption1.getText().toString().trim();
            String option2 = editOption2.getText().toString().trim();
            String option3 = editOption3.getText().toString().trim();
            String option4 = editOption4.getText().toString().trim();

            if (question.isEmpty()) {
                editQuestion.setError("Question is required");
                return;
            }

            if (option1.isEmpty() || option2.isEmpty()) {
                Toast.makeText(getContext(), "At least 2 options are required", Toast.LENGTH_SHORT).show();
                return;
            }

            List<String> options = new ArrayList<>();
            options.add(option1);
            options.add(option2);
            if (!option3.isEmpty()) options.add(option3);
            if (!option4.isEmpty()) options.add(option4);

            String pollId = UUID.randomUUID().toString();
            Poll poll = new Poll(
                    pollId, question, options, currentUserId, userName,
                    null, 0, switchAnonymous.isChecked(), switchMultipleVotes.isChecked()
            );

            // Initialize votes map
            Map<String, List<String>> votes = new HashMap<>();
            for (String option : options) {
                votes.put(option, new ArrayList<>());
            }
            poll.setVotes(votes);

            db.collection("polls").document(pollId)
                    .set(poll)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Poll created", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadPolls();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Failed to create poll", Toast.LENGTH_SHORT).show();
                    });
        });

        dialog.show();
    }

    @Override
    public void onVote(Poll poll, String option) {
        // Add user vote to Firebase
        db.collection("polls").document(poll.getPollId())
                .update("votes." + option, FieldValue.arrayUnion(currentUserId))
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Vote recorded", Toast.LENGTH_SHORT).show();
                    loadPolls();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to vote", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

