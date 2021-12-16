package com.sszabo.life_tok.ui.create.post;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.security.KeyChainException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sszabo.life_tok.LifeTokApplication;
import com.sszabo.life_tok.MainViewModel;
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
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

/**
 * Fragment class for posting an event. Contains all information/interactions for posting an event.
 */
public class PostFragment extends Fragment {
    private static final String TAG = PostFragment.class.getSimpleName();

    private FragmentPostBinding binding;

    private Button btnDelete;
    private Button btnPost;
    private CheckBox checkBoxPublic;
    private ImageButton btnRefreshLocation;
    private ProgressBar progressBar;
    private EditText txtEventName;
    private EditText txtEventDescription;
    private EditText txtEventLocation;
    private VideoView videoView;
    private ImageView imageView;

    private String eventName;
    private String eventDescription;
    private String eventLocation;
    private boolean isPicture;

    private GeoPoint eventGeoPoint;

    private String filePath;

    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;
    private ActivityResultLauncher<String[]> activityResultLauncher;

    /**
     * Creates the view for the post fragment. Sets up bindings and listeners.
     *
     * @param inflater           the layout inflater
     * @param container          the View Group container
     * @param savedInstanceState saved state bundle
     * @return root binding
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPostBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        filePath = getArguments().getString(Resources.KEY_FILE_PATH);
        isPicture = getArguments().getBoolean(Resources.KEY_IS_PICTURE);

        btnDelete = binding.btnDeletePost;
        btnPost = binding.btnEventPost;
        checkBoxPublic = binding.chkPublicPost;
        btnRefreshLocation = binding.btnRefreshLocationPost;
        progressBar = binding.progressBarPost;
        txtEventName = binding.txtEventNamePost;
        txtEventDescription = binding.txtEventDescriptionPost;
        txtEventLocation = binding.txtEventLocationPost;
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
                            Toast.makeText(getContext(), "Enable permissions for functionality", Toast.LENGTH_SHORT).show();
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
            videoView.setVideoPath(filePath);
            videoView.start();
        }

        setListeners();

        // required for top back button functionality
        setHasOptionsMenu(true);

        // set up back button press action
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                File file = new File(filePath);
                if (!file.delete()) {
                    Toast.makeText(getContext(), "Failed to delete file. Delete manually", Toast.LENGTH_SHORT).show();
                }
                navToCreateFragment();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), callback);

        return root;
    }

    /**
     * Starts the fragment, called when fragment is visible to the user.
     */
    @Override
    public void onStart() {
        super.onStart();
        getLocationOfEvent();
    }

    /**
     * Selector for menu options.
     *
     * @param item that was clicked
     * @return false for normal menu processing, true if handled privately
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Gets the location of an event by taking you current location.
     */
    private void getLocationOfEvent() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        activityResultLauncher.launch(permissions);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                // permission not granted return
                Toast.makeText(getContext(), "Enable permissions for functionality ", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(getContext(), "Unable to get location. Try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Sets listeners for the interactive UI elements.
     */
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

        btnRefreshLocation.setOnClickListener(new View.OnClickListener() {
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
                    eventLocation = txtEventLocation.getText().toString();
                    if (eventLocation.isEmpty()) {
                        txtEventLocation.setError("Required field");
                        Toast.makeText(getContext(), "Invalid fields!", Toast.LENGTH_SHORT).show();
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

                    if (addresses == null || addresses.isEmpty()) {
                        Toast.makeText(getContext(), "Could not find location. Try again.", Toast.LENGTH_SHORT).show();
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
                mp.start();
            }
        });

        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                } else {
                    videoView.start();
                }
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

    /**
     * Handles the posting of the new event
     */
    private void postEvent() {
        txtEventLocation.clearFocus();
        if (!setAndVerifyFields()) {
            Toast.makeText(getContext(), "Invalid fields!", Toast.LENGTH_SHORT).show();
            return;
        }
        progressBar.setVisibility(View.VISIBLE);

        String uid = FirebaseUtil.getAuth().getCurrentUser().getUid();

        // upload file to Firebase Storage
        Uri file = Uri.fromFile(new File(filePath));
        String uploadPath = "userMedia/" + uid + "/" + file.getLastPathSegment();
        StorageReference storageReference = FirebaseUtil.getStorage().getReference(uploadPath);
        Executor exec = ((LifeTokApplication) getActivity().getApplication()).executorService;

        storageReference.putFile(file).addOnCompleteListener(exec, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    uploadEventObject(uid, storageReference);
                } else {
                    Toast.makeText(getContext(), "Error uploading media", Toast.LENGTH_SHORT).show();
                    Objects.requireNonNull(task.getException()).printStackTrace();
                    progressBar.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    /**
     * Uploads the event object to Firestore and updates the public event list field for users if the event is public
     *
     * @param uid    User ID
     * @param stoRef Storage Reference to the media
     */
    private void uploadEventObject(String uid, StorageReference stoRef) {
        DocumentReference docRef = FirebaseUtil.getFirestore().collection("publicEvents").document();

        Event event = new Event(docRef.getId(),
                uid,
                MainViewModel.getCurrentUser().getUsername(),
                eventName,
                eventDescription,
                0,
                stoRef.toString(),
                "",
                isPicture,
                checkBoxPublic.isChecked() ? 1 : 0,
                eventGeoPoint,
                eventLocation,
                new Timestamp(Calendar.getInstance().getTime()));

        if (event.getEventType() == 1) {
            // public event upload
            docRef.set(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        // update user public events field
                        List<String> eventsList = MainViewModel.getCurrentUser().getPublicEventIds();
                        eventsList.add(event.getId());
                        FirebaseUtil.getFirestore()
                                .collection("users")
                                .document(uid)
                                .update("publicEventIds", eventsList)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getContext(), "Uploaded event",
                                                    Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.INVISIBLE);
                                            navToCreateFragment();
                                        } else {
                                            Toast.makeText(getContext(), "Failed to update user events",
                                                    Toast.LENGTH_SHORT).show();
                                            deleteMediaFromDB(stoRef);
                                            Objects.requireNonNull(task.getException()).printStackTrace();
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(getContext(), "Error uploading event", Toast.LENGTH_SHORT).show();
                        deleteMediaFromDB(stoRef);
                        Objects.requireNonNull(task.getException()).printStackTrace();
                    }
                }
            });
        } else {
            // private event upload
            docRef = FirebaseUtil.getFirestore()
                    .collection("users")
                    .document(uid)
                    .collection("events")
                    .document();

            event.setId(docRef.getId());

            docRef.set(event).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Uploaded event", Toast.LENGTH_SHORT).show();
                        progressBar.setVisibility(View.INVISIBLE);
                        navToCreateFragment();
                    } else {
                        Toast.makeText(getContext(), "Error uploading event", Toast.LENGTH_SHORT).show();
                        deleteMediaFromDB(stoRef);
                        Objects.requireNonNull(task.getException()).printStackTrace();
                    }
                }
            });
        }
    }

    /**
     * Deletes the media file from Firebase Storage and makes the progress bar invisible
     *
     * @param ref the storage reference to the media
     */
    private void deleteMediaFromDB(StorageReference ref) {
        ref.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    Toast.makeText(getContext(), "Error deleting media", Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
            }
        });
    }

    /**
     * Sets the instance variables with the contents of updated UI elements and ensures all are valid.
     *
     * @return true if all fields are valid, false otherwise
     */
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

    /**
     * Navigate to the Create Fragment
     */
    private void navToCreateFragment() {
        NavHostFragment.findNavController(PostFragment.this).navigate(R.id.action_nav_post_to_nav_create);
    }

    /**
     * Destroys the fragment, called when fragment is no longer in use.
     */
    @Override
    public void onDestroy() {
        // user navigates away from post fragment, should delete temp file of post
        File file = new File(filePath);
        if (!file.delete()) {
            Toast.makeText(getContext(), "Failed to delete file, delete manually", Toast.LENGTH_SHORT).show();
        }

        super.onDestroy();
        binding = null;
    }
}
