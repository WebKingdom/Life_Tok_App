package com.sszabo.life_tok.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        return new ProfileViewHolder(LayoutInflater.from(context).inflate(R.layout.profile_info_row, parent, false));
    }

    // populate data into the view item through the holder
    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User user = MainViewModel.getCurrentUser();
        holder.usernameText.setText(user.getUsername());
        holder.numFollowingText.setText(Integer.toString(user.getFollowing().size()));
        holder.numFollowersText.setText(Integer.toString(user.getFollowers().size()));
    }

    private void getUserData(ProfileViewHolder holder) {
        FirebaseUser fUser = FirebaseUtil.getAuth().getCurrentUser();

        FirebaseUtil.getFirestore().collection("users").document(fUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            try {
                                User user;
                                user = task.getResult().toObject(User.class);
                                DocumentSnapshot document = task.getResult();
                                user.setFollowers((List<String>) document.get("followers"));
                                user.setFollowing((List<String>) document.get("following"));

                                holder.usernameText.setText(user.getUsername());
                                holder.numFollowingText.setText(Integer.toString(user.getFollowing().size()));
                                holder.numFollowersText.setText(Integer.toString(user.getFollowers().size()));
                            } catch (Exception e) {
                                Toast.makeText(holder.itemView.getContext(), "User data found in DB but null",
                                        Toast.LENGTH_LONG).show();
                                e.printStackTrace();
                            }
                        } else {
                            Toast.makeText(holder.itemView.getContext(), "Could not find user data in DB",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return 1;
    }


    public class ProfileViewHolder extends RecyclerView.ViewHolder {

        private ImageView profileImageView;
        private TextView usernameText;
        private TextView numFollowersText;
        private TextView numFollowingText;

        public ProfileViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.image_view_profile);
            usernameText = itemView.findViewById(R.id.txt_post_name);
            numFollowersText = itemView.findViewById(R.id.txt_num_followers);
            numFollowingText = itemView.findViewById(R.id.txt_num_following);
        }
    }
}
