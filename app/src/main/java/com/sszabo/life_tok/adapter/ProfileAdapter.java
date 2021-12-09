package com.sszabo.life_tok.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {
    private static final String TAG = ProfileAdapter.class.getSimpleName();

    private ArrayList<Event> eventsList;
    private Context context;

    public ProfileAdapter(ArrayList<Event> events){
        eventsList = events;
    }

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new ProfileViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_profile_row, parent, false));
    }

    // populate data into the view item through the holder
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        holder.txtEventName.setText(eventsList.get(position).getName());
        holder.txtEventDescription.setText(eventsList.get(position).getDescription());
        holder.txtEventLocation.setText(eventsList.get(position).getLocationName());
        holder.progressBar.setVisibility(View.VISIBLE);

        holder.btnEventDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO delete event
            }
        });

        holder.eventVideoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (holder.eventVideoView.isPlaying()) {
                    holder.eventVideoView.pause();
                } else {
                    holder.eventVideoView.start();
                }
            }
        });

        downloadEventMedia(eventsList.get(position), holder);
    }

    private void downloadEventMedia(Event event, ProfileViewHolder holder) {
        final long HUNDRED_MEGABYTE = 100 * 1024 * 1024;
        StorageReference ref = FirebaseUtil.getStorage().getReferenceFromUrl(event.getMediaUrl());

        ref.getBytes(HUNDRED_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    File temp = null;
                    try {
                        // write to temporary file
                        File outputDir = context.getCacheDir();
                        String suffix = event.isPicture() ? ".jpg" : ".mp4";
                        temp = File.createTempFile(event.getId(), suffix, outputDir);
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(task.getResult());
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (temp != null) {
                        if (!temp.exists()) {
                            // TODO
                            Log.d(TAG, "onComplete: Temp file does not exist! Create new one?");
                        }
                        if (event.isPicture()) {
                            // display picture
                            holder.eventVideoView.setVisibility(View.INVISIBLE);
                            holder.eventImageView.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            holder.eventImageView.setImageURI(Uri.fromFile(temp));
                        } else {
                            // display video
                            holder.eventVideoView.setVisibility(View.VISIBLE);
                            holder.eventImageView.setVisibility(View.INVISIBLE);
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            Uri uri = Uri.fromFile(temp);
                            holder.eventVideoView.setVideoURI(uri);
                            holder.eventVideoView.start();
                        }
                        temp.deleteOnExit();
                    } else {
                        Toast.makeText(context, "Temporary media save failed", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onComplete: Temporary media save failed");
                    }

                } else {
                    Toast.makeText(context, "Could not get event media", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onComplete: Could not get event media");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return eventsList.size();
    }


    public class ProfileViewHolder extends RecyclerView.ViewHolder {

        private ImageView eventImageView;
        private VideoView eventVideoView;
        private ProgressBar progressBar;
        private TextView txtEventName;
        private TextView txtEventDescription;
        private TextView txtEventLocation;
        private ImageButton btnEventDelete;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImageView = itemView.findViewById(R.id.image_view_event_profile);
            eventVideoView = itemView.findViewById(R.id.video_view_event_profile);
            progressBar = itemView.findViewById(R.id.progress_bar_profile);
            txtEventName = itemView.findViewById(R.id.txt_event_name_profile);
            txtEventDescription = itemView.findViewById(R.id.txt_event_description_profile);
            txtEventLocation = itemView.findViewById(R.id.txt_event_location_profile);
            btnEventDelete = itemView.findViewById(R.id.btn_delete_event_profile);
        }
    }
}
