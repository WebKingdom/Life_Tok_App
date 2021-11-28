package com.sszabo.life_tok.ui.post;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavOptions;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentPostBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.util.FirebaseUtil;
import com.sszabo.life_tok.util.Resources;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PostFragment extends Fragment {
    private static final String TAG = PostFragment.class.getSimpleName();

    private FragmentPostBinding binding;
    private PostViewModel postViewModel;

    private Button btnDelete;
    private Button btnPost;
    private CheckBox checkBoxPublic;
    private ImageButton btnRefresh;
    private ProgressBar progressBar;
    private EditText txtEventName;
    private EditText txtEventDescription;
    private EditText txtEventLocation;
    private VideoView videoView;
    private ImageView imageView;
    private MediaController mediaController;

    private String eventName;
    private String eventDescription;
    private String eventLocation;

    private GeoPoint eventGeoPoint;

    private String filePath;

    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;
    private ActivityResultLauncher<String[]> activityResultLauncher;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        postViewModel = new PostViewModel();

        binding = FragmentPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        filePath = getArguments().getString(Resources.KEY_FILE_PATH);
        boolean isPicture = getArguments().getBoolean(Resources.KEY_IS_PICTURE);

        btnDelete = binding.btnDeletePost;
        btnPost = binding.btnPost;
        checkBoxPublic = binding.chkPublic;
        btnRefresh = binding.btnRefreshLocation;
        progressBar = binding.progressBarPost;
        txtEventName = binding.txtEventName;
        txtEventDescription = binding.txtEventDescription;
        txtEventLocation = binding.txtEventLocation;
        videoView = binding.videoViewPost;
        imageView = binding.imageViewPost;

        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> result) {
                        boolean allGranted = true;

                        for (boolean b : result.values()) {
                            allGranted = allGranted && b;
                        }

                        if (allGranted) {
                            Log.d(TAG, "onActivityResult: All permissions granted");
                        } else {
                            Toast.makeText(getContext(), "Must enable permissions for functionality", Toast.LENGTH_LONG).show();
                            onPause();
                            onStop();
                        }
                    }
                });

        if (isPicture) {
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

    @Override
    public void onResume() {
        super.onResume();
        getLocationOfEvent();
    }

    private void getLocationOfEvent() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        activityResultLauncher.launch(permissions);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                // permission not granted return
                Toast.makeText(getContext(), "Must enable permissions for functionality ", Toast.LENGTH_LONG).show();
                onPause();
                onStop();
                return;
            }
        }

        geocoder = new Geocoder(getContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getContext());

        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    Location loc = task.getResult();
                    eventGeoPoint = new GeoPoint(loc.getLatitude(), loc.getLongitude());
                    Log.d(TAG, "onComplete: Location is: " + loc.toString());

                    List<Address> searchAddr = new ArrayList<>();
                    try {
                        searchAddr = geocoder.getFromLocation(eventGeoPoint.getLatitude(),
                                eventGeoPoint.getLongitude(),
                                2);
                    } catch (IOException e) {
                        Log.e(TAG, "onComplete: IOException " + e.getMessage() + "\n", e);
                        e.printStackTrace();
                        return;
                    }
                    // Display location in edit text
                    txtEventLocation.setText(searchAddr.get(0).getAddressLine(0));
                } else {
                    Toast.makeText(getContext(), "Unable to get location. Try again.", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setListeners() {
        btnPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postEvent();
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

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocationOfEvent();
            }
        });

        txtEventLocation.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    // user edited text, must get location
                    if (!setAndVerifyFields()) {
                        Toast.makeText(getContext(), "Invalid fields!", Toast.LENGTH_LONG).show();
                        return;
                    }

                    List<Address> addresses;
                    try {
                        addresses = geocoder.getFromLocationName(eventLocation, 2);
                    } catch (IOException e) {
                        Toast.makeText(getContext(), "Could not get location. Try again", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        return;
                    }

                    Address addr = addresses.get(0);
                    eventGeoPoint = new GeoPoint(addr.getLatitude(), addr.getLongitude());
                    txtEventLocation.setText(addr.getAddressLine(0));
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

    private void postEvent() {
        if (!setAndVerifyFields()) {
            Toast.makeText(getContext(), "Invalid fields!", Toast.LENGTH_LONG).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        // upload file to Firebase Storage
        Uri file = Uri.fromFile(new File(filePath));
        StorageReference storageReference = FirebaseUtil.getStorage()
                .getReference()
                .child("userMedia/" + file.getLastPathSegment());
        UploadTask uploadTask = storageReference.putFile(file);

        uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    Event event = new Event();
                    event.setMediaUrl(storageReference.getDownloadUrl().toString());

                    event.setName(txtEventName.getText().toString());
                    event.setDescription(txtEventDescription.getText().toString());
                    event.setGeoPoint(eventGeoPoint);
                    event.setEventType(checkBoxPublic.isChecked() ? 1 : 0);
                    event.setTimestamp(new Timestamp(Calendar.getInstance().getTime()));

                    if (event.getEventType() == 1) {
                        // public event upload
                        FirebaseUtil.getFirestore()
                                .collection("publicEvents")
                                .add(event)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Uploaded event", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            navToCreateFragment();
                                        } else {
                                            Toast.makeText(getContext(), "Error uploading event", Toast.LENGTH_SHORT).show();
                                            // delete from storage
                                            deleteMediaFromDB(storageReference);
                                            Objects.requireNonNull(task.getException()).printStackTrace();
                                        }
                                    }
                                });
                    } else {
                        // private event upload
                        FirebaseUser fUser = FirebaseUtil.getAuth().getCurrentUser();
                        FirebaseUtil.getFirestore()
                                .collection("users")
                                .document(fUser.getUid())
                                .collection("events")
                                .add(event)
                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Uploaded event", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            navToCreateFragment();
                                        } else {
                                            Toast.makeText(getContext(), "Error uploading event", Toast.LENGTH_SHORT).show();
                                            // delete from storage
                                            deleteMediaFromDB(storageReference);
                                            Objects.requireNonNull(task.getException()).printStackTrace();
                                        }
                                    }
                                });
                    }

                } else {
                    Toast.makeText(getContext(), "Error uploading media", Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(task.getException()).printStackTrace();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void deleteMediaFromDB(StorageReference ref) {
        ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getContext(), "Deleted media", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "Error deleting media", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    private boolean setAndVerifyFields() {
        boolean valid = true;

        eventName = txtEventName.getText().toString();
        eventDescription = txtEventDescription.getText().toString();
        eventLocation = txtEventLocation.getText().toString();

        if (eventName.isEmpty()) {
            valid = false;
            txtEventName.setError("Required field");
        }

        if (eventDescription.isEmpty()) {
            valid = false;
            txtEventDescription.setError("Required field");
        }

        if (eventLocation.isEmpty()) {
            valid = false;
            txtEventLocation.setError("Required field");
        }

        return valid;
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
