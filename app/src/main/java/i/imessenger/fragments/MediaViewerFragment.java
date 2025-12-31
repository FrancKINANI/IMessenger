package i.imessenger.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;

import i.imessenger.databinding.FragmentMediaViewerBinding;

public class MediaViewerFragment extends Fragment {

    private FragmentMediaViewerBinding binding;
    private String mediaUrl;
    private String mediaType;
    private boolean isPlaying = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentMediaViewerBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getArguments() != null) {
            mediaUrl = getArguments().getString("mediaUrl");
            mediaType = getArguments().getString("mediaType", "image");
        }

        if (mediaUrl == null) {
            NavHostFragment.findNavController(this).navigateUp();
            return;
        }

        setupToolbar();
        loadMedia();
    }

    private void setupToolbar() {
        binding.toolbar.setNavigationOnClickListener(v ->
                NavHostFragment.findNavController(this).navigateUp());
    }

    private void loadMedia() {
        binding.progressBar.setVisibility(View.VISIBLE);

        if ("video".equals(mediaType)) {
            loadVideo();
        } else {
            loadImage();
        }
    }

    private void loadImage() {
        binding.ivMedia.setVisibility(View.VISIBLE);
        binding.videoView.setVisibility(View.GONE);
        binding.ivPlayPause.setVisibility(View.GONE);

        Glide.with(this)
                .load(mediaUrl)
                .into(binding.ivMedia);

        binding.progressBar.setVisibility(View.GONE);
    }

    private void loadVideo() {
        binding.ivMedia.setVisibility(View.GONE);
        binding.videoView.setVisibility(View.VISIBLE);
        binding.ivPlayPause.setVisibility(View.VISIBLE);

        MediaController mediaController = new MediaController(requireContext());
        mediaController.setAnchorView(binding.videoView);
        binding.videoView.setMediaController(mediaController);
        binding.videoView.setVideoURI(Uri.parse(mediaUrl));

        binding.videoView.setOnPreparedListener(mp -> {
            binding.progressBar.setVisibility(View.GONE);
            mp.setLooping(true);
        });

        binding.videoView.setOnErrorListener((mp, what, extra) -> {
            binding.progressBar.setVisibility(View.GONE);
            return false;
        });

        binding.ivPlayPause.setOnClickListener(v -> {
            if (isPlaying) {
                binding.videoView.pause();
                binding.ivPlayPause.setVisibility(View.VISIBLE);
            } else {
                binding.videoView.start();
                binding.ivPlayPause.setVisibility(View.GONE);
            }
            isPlaying = !isPlaying;
        });

        binding.videoView.setOnClickListener(v -> {
            if (isPlaying) {
                binding.videoView.pause();
                binding.ivPlayPause.setVisibility(View.VISIBLE);
                isPlaying = false;
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        if (binding != null && binding.videoView.isPlaying()) {
            binding.videoView.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null && binding.videoView != null) {
            binding.videoView.stopPlayback();
        }
        binding = null;
    }
}

