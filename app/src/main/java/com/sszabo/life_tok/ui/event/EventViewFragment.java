package com.sszabo.life_tok.ui.event;

import android.annotation.SuppressLint;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentEventViewBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.util.FirebaseUtil;
import com.sszabo.life_tok.util.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Fragment class for viewing an event. Contains all information/interactions for viewing an event.
 */
public class EventViewFragment extends Fragment {
    private static final String TAG = EventViewFragment.class.getSimpleName();

    private FragmentEventViewBinding binding;

    private TextView txtEventName;
    private TextView txtEventUsername;
    private TextView txtEventDescription;
    private TextView txtEventLocation;
    private TextView txtEventTime;
    private VideoView videoView;
    private ImageView imageView;
    private ImageButton btnLocationMap;

    private Event event;

    /**
     * Creates the view for the event view fragment. Sets up bindings, listeners, and options menu.
     *
     * @param inflater           the layout inflater
     * @param container          the View Group container
     * @param savedInstanceState saved state bundle
     * @return root binding
     */
    @NonNull
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentEventViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        txtEventName = binding.txtEventViewName;
        txtEventUsername = binding.txtEventViewUsername;
        txtEventDescription = binding.txtEventViewDescription;
        txtEventLocation = binding.txtEventViewLocation;
        txtEventTime = binding.txtEventViewTime;
        videoView = binding.videoViewEvent;
        imageView = binding.imageViewEvent;
        btnLocationMap = binding.btnShowLocationMap;

        event = (Event) getArguments().getSerializable(Resources.KEY_EVENT);

        if (event.isPicture()) {
            videoView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        }

        setListeners();

        // required for top back button functionality
        setHasOptionsMenu(true);

        // set up back button press action
        OnBackPressedCallback backPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                navToMapFragment();
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), backPressedCallback);

        return root;
    }

    /**
     * Starts the fragment. Downloads the event media and displays all event information.
     */
    @Override
    public void onStart() {
        super.onStart();

        final long HUNDRED_MEGABYTE = 100 * 1024 * 1024;
        StorageReference ref = FirebaseUtil.getStorage().getReferenceFromUrl(event.getMediaUrl());

        ref.getBytes(HUNDRED_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    File temp = null;
                    try {
                        // write to temporary file
                        File outputDir = getContext().getCacheDir();
                        String suffix = event.isPicture() ? ".jpg" : ".mp4";
                        temp = File.createTempFile(event.getId(), suffix, outputDir);
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(task.getResult());
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (temp != null) {
                        if (event.isPicture()) {
                            // display picture
                            imageView.setImageURI(Uri.fromFile(temp));
                        } else {
                            // display video
                            Uri uri = Uri.fromFile(temp);
                            videoView.setVideoURI(uri);
                            videoView.start();
                        }
                        temp.deleteOnExit();
                    } else {
                        Toast.makeText(getContext(), "Temporary media save failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Could not get event media", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtEventName.setText(event.getName());
        txtEventUsername.setText("@" + event.getUsername());
        txtEventDescription.setText(event.getDescription());
        txtEventLocation.setText(event.getLocationName());
        txtEventTime.setText(event.getTimestamp().toDate().toString());
    }

    /**
     * Resumes the fragment, called when it is visible to user and is running.
     */
    @Override
    public void onResume() {
        super.onResume();

        if (isVisible()) {
            getActivity().setTitle(event.getName());
        }
    }

    /**
     * Sets listeners for interactive UI elements.
     */
    private void setListeners() {
        btnLocationMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navToMapFragment();
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
     * Navigates to the Map Fragment
     */
    private void navToMapFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Resources.KEY_EVENT, event);

        NavHostFragment.findNavController(EventViewFragment.this)
                .navigate(R.id.action_nav_event_view_to_nav_map, bundle);
    }

    /**
     * Destroys the fragment, called when fragment is no longer in use.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
