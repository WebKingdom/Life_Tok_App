package com.sszabo.life_tok.ui.event;

import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.exoplayer2.text.span.HorizontalTextInVerticalContextSpan;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.StorageReference;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentEventViewBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.util.FirebaseUtil;
import com.sszabo.life_tok.util.Resources;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class EventViewFragment extends Fragment {
    private static final String TAG = EventViewFragment.class.getSimpleName();

    private EventViewModel eventViewModel;
    private FragmentEventViewBinding binding;

    private TextView txtEventName;
    private TextView txtEventDescription;
    private TextView txtEventLocation;
    private VideoView videoView;
    private ImageView imageView;
    private ImageButton btnLocationMap;

    private Event event;
    private MediaController mediaController;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        eventViewModel = new ViewModelProvider(this).get(EventViewModel.class);
        binding = FragmentEventViewBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        txtEventName = binding.txtEventViewName;
        txtEventDescription = binding.txtEventViewDescription;
        txtEventLocation = binding.txtEventViewLocation;
        videoView = binding.videoViewEvent;
        imageView = binding.imageViewEvent;
        btnLocationMap = binding.btnShowLocationMap;

        event = (Event) getArguments().getSerializable(Resources.KEY_EVENT);

        if (event.isPicture()) {
            videoView.setVisibility(View.INVISIBLE);
            imageView.setVisibility(View.VISIBLE);
        } else {
            mediaController = new MediaController(getContext());
            mediaController.setAnchorView(videoView);

            videoView.setMediaController(mediaController);
        }

        setListeners();

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();

        final long HUNDRED_MEGABYTE = 100 * 1024 * 1024;
//        String uid = FirebaseUtil.getAuth().getUid();
//        String downloadPath = "userMedia/" + uid + "/" + event.getTimestamp();
//        if (event.isPicture()) {
//            downloadPath = downloadPath + ".jpeg";
//        } else {
//            downloadPath = downloadPath + ".mp4";
//        }
        StorageReference ref = FirebaseUtil.getStorage().getReferenceFromUrl(event.getMediaUrl());

        ref.getBytes(HUNDRED_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    File temp = null;
                    try {
                        // write to temporary file
                        temp = File.createTempFile(event.getUserId(), ".mp4");
                        temp.deleteOnExit();
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(task.getResult());

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (temp != null) {
                        if (event.isPicture()) {
                            // TODO display picture
                            imageView.setImageURI(Uri.fromFile(temp));
                        } else {
                            // TODO display video
                            videoView.setVideoPath(temp.getAbsolutePath());
                            videoView.start();
                        }
                    } else {
                        Toast.makeText(getContext(), "Temporary media save failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Could not get event media", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtEventName.setText(event.getName());
        txtEventDescription.setText(event.getDescription());
        txtEventLocation.setText(event.getLocationName());
    }

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

    private void navToMapFragment() {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Resources.KEY_EVENT, event);

        NavHostFragment.findNavController(EventViewFragment.this)
                .navigate(R.id.action_nav_event_view_to_nav_map, bundle);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
