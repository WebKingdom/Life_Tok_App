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
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ViewHolder> {

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // inflate custom layout
        View profileView = inflater.inflate(R.layout.profile_info_row, parent, false);

        // return new view holder instance
        ViewHolder viewHolder = new ViewHolder(profileView);
        return viewHolder;
    }

    // populate data into the item through holder
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirebaseUser fUser = FirebaseUtil.getAuth().getCurrentUser();
        User curUser = new User();
        curUser.setUsername(fUser.getDisplayName());

        FirebaseUtil.getFirestore().collection("users").document(fUser.getUid()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    try {
                        DocumentSnapshot document = task.getResult();
                        curUser.setFollowers((List<String>) document.get("followers"));
                        curUser.setFollowing((List<String>) document.get("following"));

                        holder.usernameText.setText(curUser.getUsername());
                        holder.numFollowingText.setText(Integer.toString(curUser.getFollowing().size()));
                        holder.numFollowersText.setText(Integer.toString(curUser.getFollowers().size()));
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView profileImageView;
        private TextView usernameText;
        private TextView numFollowersText;
        private TextView numFollowingText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            profileImageView = itemView.findViewById(R.id.profileImageView);
            usernameText = itemView.findViewById(R.id.txt_username);
            numFollowersText = itemView.findViewById(R.id.txtNumFollowers);
            numFollowingText = itemView.findViewById(R.id.txtNumFollowing);
        }
    }
}
