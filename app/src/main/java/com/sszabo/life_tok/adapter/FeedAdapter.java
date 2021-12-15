package com.sszabo.life_tok.adapter;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.RequestManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.storage.StorageReference;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Feed Adapter class that extends Recycler View for handling and displaying the scrollable feed of events
 * on the user's main page.
 */
public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {
    private static final String TAG = FeedAdapter.class.getSimpleName();

    private ArrayList<Event> eventsList;
    private RequestManager requestManager;
    private Context context;

    /**
     * Constructor for the feed adapter.
     *
     * @param events         list of events (from the users that are being followed)
     * @param requestManager request manager for Glide (for setting profile profile picture on feed)
     */
    public FeedAdapter(ArrayList<Event> events, RequestManager requestManager) {
        this.eventsList = events;
        this.requestManager = requestManager;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new FeedViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_feed, parent, false));
    }

    /**
     * Binds the elements in the holder to an event specified by the position (index).
     * @param holder the ViewHolder to bind to
     * @param position the index in the list of events
     */
    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.txtEventName.setText(eventsList.get(position).getName());
        holder.txtDescription.setText(eventsList.get(position).getDescription());
        holder.txtLocation.setText(eventsList.get(position).getLocationName());
        holder.txtNumLikes.setText(Integer.toString(eventsList.get(position).getNumLikes()));
        // set profile picture image
        requestManager.load(eventsList.get(position).getThumbnailUrl()).into(holder.profilePicOption);

        // set follow button listener
        holder.followOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO implement follow user
            }
        });

        downloadAndStartMedia(eventsList.get(position), holder);

        // restart video when finished/get to end
        holder.videoPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mp.start();
            }
        });

        holder.videoPlayer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!eventsList.get(holder.getAbsoluteAdapterPosition()).isPicture() && holder.videoPlayer.isPlaying()) {
                    holder.videoPlayer.pause();
                } else if (!eventsList.get(holder.getAbsoluteAdapterPosition()).isPicture()) {
                    holder.videoPlayer.start();
                }
            }
        });

        holder.videoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(context, "Error playing back video", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

    /**
     * Downloads the correct event media and displays it.
     *
     * @param event  the event to download the media for
     * @param holder the view holder to put the media in
     */
    private void downloadAndStartMedia(Event event, FeedViewHolder holder) {
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
                            // TODO? temp file does not exist
                            Log.d(TAG, "onComplete: Temp file does not exist! Create new one?");
                        }
                        if (event.isPicture()) {
                            // display picture
                            holder.videoPlayer.setVisibility(View.INVISIBLE);
                            holder.imageView.setVisibility(View.VISIBLE);
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            holder.imageView.setImageURI(Uri.fromFile(temp));
                        } else {
                            // display video
                            holder.videoPlayer.setVisibility(View.VISIBLE);
                            holder.imageView.setVisibility(View.INVISIBLE);
                            holder.progressBar.setVisibility(View.INVISIBLE);
                            Uri uri = Uri.fromFile(temp);
                            holder.videoPlayer.setVideoURI(uri);
                            holder.videoPlayer.start();
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


    /**
     * View Holder class for the Feed Adapter so that the items in the view can be accessed.
     */
    public class FeedViewHolder extends RecyclerView.ViewHolder {

        private VideoView videoPlayer;
        private ImageView imageView;
        private ProgressBar progressBar;
        private TextView txtEventName;
        private TextView txtDescription;
        private TextView txtLocation;
        private TextView txtNumLikes;
        private ShapeableImageView profilePicOption;
        private ImageView followOption;

        public FeedViewHolder(@NonNull View itemView) {
            super(itemView);
            videoPlayer = itemView.findViewById(R.id.player_view_feed);
            imageView = itemView.findViewById(R.id.image_view_feed);
            progressBar = itemView.findViewById(R.id.progress_bar_feed);
            txtEventName = itemView.findViewById(R.id.txt_post_name);
            txtDescription = itemView.findViewById(R.id.txt_post_description);
            txtLocation = itemView.findViewById(R.id.txt_post_location);
            txtNumLikes = itemView.findViewById(R.id.txt_num_likes);
            profilePicOption = itemView.findViewById(R.id.image_view_profile_pic);
            followOption = itemView.findViewById(R.id.image_view_follow_option);
        }
    }
}
