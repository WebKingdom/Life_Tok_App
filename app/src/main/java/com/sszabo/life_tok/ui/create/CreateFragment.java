package com.sszabo.life_tok.ui.create;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.core.VideoCapture;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentCreateBinding;
import com.sszabo.life_tok.util.Resources;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public class CreateFragment extends Fragment {

    private static final String TAG = CreateFragment.class.getSimpleName();

    private CreateViewModel createViewModel;
    private FragmentCreateBinding binding;

    private PreviewView previewView;
    private ImageCapture imageCapture;
    private VideoCapture videoCapture;
    private Camera camera;

    private ImageView btnPhoto;
    private ImageView btnVideo;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private ActivityResultLauncher<String[]> activityResultLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        createViewModel =
                new ViewModelProvider(this).get(CreateViewModel.class);

        binding = FragmentCreateBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnPhoto = binding.floatingBtnPhoto;
        btnVideo = binding.floatingBtnVideo;
        previewView = binding.previewViewCamera;

        setListener();

        // set up permission requests
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        boolean allGranted = true;
                        for (boolean b : result.values()) {
                            allGranted = allGranted && b;
                        }

                        if (allGranted) {
                            // can now take photo or video
                            Log.d(TAG, "onActivityResult: All permissions granted");
                        } else {
                            Toast.makeText(getContext(), "Enable permission for functionality", Toast.LENGTH_SHORT).show();
                            onPause();
                            onStop();
                        }
                    }
                });

        // Add listener for camera and bind to lifecycle
        cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
        cameraProviderFuture.addListener(() -> {
            try {
                ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                bindPreview(cameraProvider);
            } catch (ExecutionException | InterruptedException e) {
                // should not need error handling
                e.printStackTrace();
            }
        }, ContextCompat.getMainExecutor(getContext()));


//        final TextView textView = binding.cardViewGallery;
//        createViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
//            @Override
//            public void onChanged(@Nullable String s) {
//                textView.setText(s);
//            }
//        });
        return root;
    }

    @SuppressLint("RestrictedApi")
    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        cameraProvider.unbindAll();

        Preview preview = new Preview.Builder().build();

        imageCapture = new ImageCapture.Builder()
                .setMaxResolution(new Size (1920, 1080))
                .build();

        videoCapture = new VideoCapture.Builder()
                .setVideoFrameRate(30)
                .setMaxResolution(new Size(1920, 1080))
                .build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageCapture, videoCapture);
    }

    @Override
    public void onStart() {
        super.onStart();
        String[] permissions = new String[]{Manifest.permission.CAMERA,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO};

        activityResultLauncher.launch(permissions);
    }

    private void setListener() {
        btnPhoto.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                capturePicture();
            }
        });

        btnVideo.setOnClickListener(new View.OnClickListener() {
            @SuppressLint({"RestrictedApi", "ResourceAsColor"})
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (createViewModel.isRecording()) {
                    btnVideo.setBackgroundTintList(ColorStateList.valueOf(R.color.white_smoke));
                    videoCapture.stopRecording();
                    createViewModel.setRecording(false);
                } else {
                    createViewModel.setRecording(true);
                    btnVideo.setBackgroundTintList(ColorStateList.valueOf(R.color.light_grey));
                    recordVideo();
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void capturePicture() {
        String filePath = getOutputFile();

        if (filePath == null) {
            return;
        }

        File pictureFile = new File(filePath + ".jpg");

        imageCapture.takePicture(
                new ImageCapture.OutputFileOptions.Builder(pictureFile).build(),
                ContextCompat.getMainExecutor(getContext()),
                new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "onImageSaved: Saved to: " + filePath);

                        navToPostFragment(pictureFile.getAbsolutePath(), true);

//                        Toast.makeText(getContext(), "Saved to: " + filePath, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        Toast.makeText(getContext(), "Failed to save picture", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @SuppressLint("RestrictedApi")
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void recordVideo() {
        if (videoCapture == null) {
            return;
        }

        String filePath = getOutputFile();

        if (filePath == null) {
            return;
        }

        File videoFile = new File(filePath + ".mp4");

        videoCapture.startRecording(
                new VideoCapture.OutputFileOptions.Builder(videoFile).build(),
                ContextCompat.getMainExecutor(getContext()),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "onVideoSaved: Saved to: " + filePath);

                        navToPostFragment(videoFile.getAbsolutePath(), false);

//                        Toast.makeText(getContext(), "Saved to: " + filePath, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(int videoCaptureError, @NonNull String message, @Nullable Throwable cause) {
                        Toast.makeText(getContext(), "Failed to save video", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private String getOutputFile() {
        Optional<File> pictureOptional = Arrays.stream(getContext().getExternalMediaDirs()).findFirst();

        if (!pictureOptional.isPresent()) {
            Log.d(TAG, "capturePicture: Could not get media directory");
            Toast.makeText(getContext(), "Could not get media directory", Toast.LENGTH_SHORT).show();
            return null;
        }

        File pictureDir = pictureOptional.get();

        if (!pictureDir.exists()) {
            if (!pictureDir.mkdir()) {
                Toast.makeText(getContext(), "Could not save post. Please enable storage permissions", Toast.LENGTH_LONG).show();
            }
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        return pictureDir.getAbsolutePath() + "/" + timestamp;
    }

    /**
     * Navigates to post fragment to create the actual post
     * @param path for the media file
     * @param isPicture true if picture, false otherwise
     */
    private void navToPostFragment(String path, boolean isPicture) {
        Bundle bundle = new Bundle();
        bundle.putString(Resources.KEY_FILE_PATH, path);
        bundle.putBoolean(Resources.KEY_IS_PICTURE, isPicture);

        NavHostFragment.findNavController(CreateFragment.this)
                .navigate(R.id.action_nav_create_to_nav_post, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
