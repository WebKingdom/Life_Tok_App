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

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.FeedViewHolder> {

    private static final String TAG = FeedAdapter.class.getSimpleName();

    private ArrayList<Event> eventsList;
    private RequestManager requestManager;
    private Context context;

    private boolean isPlaying;

    public FeedAdapter(ArrayList<Event> events, RequestManager requestManager) {
        this.eventsList = events;
        this.requestManager = requestManager;
        isPlaying = false;
    }

    @NonNull
    @Override
    public FeedViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext();
        return new FeedViewHolder(LayoutInflater.from(context).inflate(R.layout.layout_feed, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FeedViewHolder holder, int position) {
        holder.txtPostName.setText(eventsList.get(position).getName());
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
                isPlaying = true;
            }
        });

//        holder.videoPlayer.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!eventsList.get(holder.getAbsoluteAdapterPosition()).isPicture() && isPlaying) {
//                    holder.videoPlayer.pause();
//                    isPlaying = false;
//                } else if (!eventsList.get(holder.getAbsoluteAdapterPosition()).isPicture()) {
//                    holder.videoPlayer.resume();
//                    isPlaying = true;
//                }
//            }
//        });

        holder.videoPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(context, "Error playing back video", Toast.LENGTH_SHORT).show();
                return false;
            }
        });
    }

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
                        temp = File.createTempFile(event.getId(), ".mp4", outputDir);
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(task.getResult());
                        temp.deleteOnExit();
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (temp != null) {
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
                            holder.videoPlayer.setVideoPath(temp.getAbsolutePath());
                            holder.videoPlayer.start();
                            isPlaying = true;
                        }
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


    public class FeedViewHolder extends RecyclerView.ViewHolder {

        private VideoView videoPlayer;
        private ImageView imageView;
        private ProgressBar progressBar;
        private TextView txtPostName;
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
            txtPostName = itemView.findViewById(R.id.txt_post_name);
            txtDescription = itemView.findViewById(R.id.txt_post_description);
            txtLocation = itemView.findViewById(R.id.txt_post_location);
            txtNumLikes = itemView.findViewById(R.id.txt_num_likes);
            profilePicOption = itemView.findViewById(R.id.image_view_profile_pic);
            followOption = itemView.findViewById(R.id.image_view_follow_option);
        }
    }
}
