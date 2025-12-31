package i.imessenger.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import i.imessenger.R;
import i.imessenger.adapters.MediaGridAdapter;
import i.imessenger.databinding.FragmentMediaListBinding;
import i.imessenger.models.MediaItem;
import i.imessenger.viewmodels.MediaViewModel;

public class MediaListFragment extends Fragment implements MediaGridAdapter.OnMediaClickListener {

    private FragmentMediaListBinding binding;
    private MediaViewModel mediaViewModel;
    private MediaGridAdapter adapter;
    private List<MediaItem> mediaItems = new ArrayList<>();

    private String userId;
    private int filterType; // 0=all, 1=images, 2=videos

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMediaListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            filterType = getArguments().getInt("filterType", 0);
        }

        mediaViewModel = new ViewModelProvider(this).get(MediaViewModel.class);

        setupRecyclerView();
        loadMedia();
    }

    private void setupRecyclerView() {
        adapter = new MediaGridAdapter(requireContext(), mediaItems, this);
        binding.recyclerViewMedia.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        binding.recyclerViewMedia.setAdapter(adapter);
    }

    private void loadMedia() {
        binding.progressBar.setVisibility(View.VISIBLE);

        switch (filterType) {
            case 1:
                mediaViewModel.getUserImages(userId).observe(getViewLifecycleOwner(), this::handleMediaResult);
                break;
            case 2:
                mediaViewModel.getUserVideos(userId).observe(getViewLifecycleOwner(), this::handleMediaResult);
                break;
            default:
                mediaViewModel.getUserMedia(userId).observe(getViewLifecycleOwner(), this::handleMediaResult);
                break;
        }
    }

    private void handleMediaResult(List<MediaItem> items) {
        binding.progressBar.setVisibility(View.GONE);

        if (items != null && !items.isEmpty()) {
            mediaItems.clear();
            mediaItems.addAll(items);
            adapter.updateMedia(mediaItems);
            binding.emptyState.setVisibility(View.GONE);
            binding.recyclerViewMedia.setVisibility(View.VISIBLE);
        } else {
            binding.emptyState.setVisibility(View.VISIBLE);
            binding.recyclerViewMedia.setVisibility(View.GONE);
        }
    }

    @Override
    public void onMediaClicked(MediaItem mediaItem, int position) {
        Bundle args = new Bundle();
        args.putString("mediaUrl", mediaItem.getUrl());
        args.putString("mediaType", mediaItem.getType());
        NavHostFragment.findNavController(this).navigate(R.id.mediaViewerFragment, args);
    }

    @Override
    public void onMediaLongClicked(MediaItem mediaItem, int position) {
        // Could show delete option for current user's media
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}

