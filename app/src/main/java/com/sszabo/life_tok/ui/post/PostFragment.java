package com.sszabo.life_tok.ui.post;

import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentPostBinding;
import com.sszabo.life_tok.ui.create.CreateFragment;

import java.io.File;
import java.util.Objects;

public class PostFragment extends Fragment {
    private static final String TAG = PostFragment.class.getSimpleName();

    private FragmentPostBinding binding;
    private PostViewModel postViewModel;

    private Button btnDelete;
    private Button btnPost;
    private VideoView videoView;
    private ImageView imageView;
    private MediaController mediaController;

    private String filePath;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        postViewModel = new PostViewModel();

        binding = FragmentPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        filePath = getArguments().getString(CreateFragment.KEY_FILE_PATH);

        btnDelete = binding.btnDeletePost;
        btnPost = binding.btnPost;
        videoView = binding.videoViewPost;
        imageView = binding.imageViewPost;


        if (filePath.contains(".jpg")) {
            // image file
            videoView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(Uri.fromFile(new File(filePath)));
        } else {
            // video file
            mediaController = new MediaController(getContext());
            mediaController.setAnchorView(videoView);

            videoView.setMediaController(mediaController);
            videoView.setVideoPath(filePath);
            videoView.start();
        }

        setListeners();

        // set up back button press action
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                File file = new File(filePath);
                if (!file.delete()) {
                    Toast.makeText(getContext(), "Failed to delete file, delete manually", Toast.LENGTH_SHORT).show();
                }
                navToCreateFragment();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return root;
    }

    private void setListeners() {
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navToCreateFragment();
            }
        });

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File file = new File(filePath);
                if (file.delete()) {
                    navToCreateFragment();
                } else {
                    Toast.makeText(getContext(), "Failed to delete file, delete manually", Toast.LENGTH_SHORT).show();
                }
            }
        });

        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // restart video playback
                videoView.start();
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(getContext(), "Error playing back video", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    private void navToCreateFragment() {
        NavHostFragment.findNavController(PostFragment.this)
                .navigate(R.id.action_navigation_post_to_navigation_create,
                        null,
                        new NavOptions.Builder()
                                .setEnterAnim(android.R.animator.fade_in)
                                .setExitAnim(android.R.animator.fade_out)
                                .build());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
