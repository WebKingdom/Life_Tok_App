package com.sszabo.life_tok.ui.create;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
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
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavDirections;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.google.common.util.concurrent.ListenableFuture;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentCreateBinding;

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
    private ImageView btnGallery;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;

    private ActivityResultLauncher<String[]> activityResultLauncher;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        createViewModel =
                new ViewModelProvider(this).get(CreateViewModel.class);

        binding = FragmentCreateBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        btnPhoto = binding.btnPhoto;
        btnVideo = binding.btnVideo;
        btnGallery = binding.btnGallery;
        previewView = binding.previewViewCamera;

        setListener();

        // set up permission requests
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        Boolean allGranted = true;
                        for (Boolean b : result.values()) {
                            allGranted = allGranted && b;
                        }

                        if (allGranted) {
                            // TODO take photo or video
                            Log.d(TAG, "onActivityResult: All permissions granted");
                        } else {
                            Toast.makeText(getContext(), "Must enable permission for functionality", Toast.LENGTH_LONG).show();
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

        imageCapture = new ImageCapture.Builder().build();

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
            @SuppressLint("RestrictedApi")
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onClick(View v) {
                if (createViewModel.isRecording()) {
                    videoCapture.stopRecording();
                    createViewModel.setRecording(false);
                } else {
                    createViewModel.setRecording(true);
                    recordVideo();
                }
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

                        // TODO launch event creation fragment
                        NavHostFragment.findNavController(CreateFragment.this)
                                .navigate(R.id.action_navigation_create_to_navigation_post,
                                        null,
                                        new NavOptions.Builder()
                                                .setEnterAnim(android.R.animator.fade_in)
                                                .setExitAnim(android.R.animator.fade_out)
                                                .build());

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

        File pictureFile = new File(filePath + ".mp4");

        videoCapture.startRecording(
                new VideoCapture.OutputFileOptions.Builder(pictureFile).build(),
                ContextCompat.getMainExecutor(getContext()),
                new VideoCapture.OnVideoSavedCallback() {
                    @Override
                    public void onVideoSaved(@NonNull VideoCapture.OutputFileResults outputFileResults) {
                        Log.d(TAG, "onVideoSaved: Saved to: " + filePath);
                        // TODO launch event creation fragment

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
            pictureDir.mkdir();
        }

        Date date = new Date();
        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String filePath = pictureDir.getAbsolutePath() + "/" + timestamp;

        return filePath;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
