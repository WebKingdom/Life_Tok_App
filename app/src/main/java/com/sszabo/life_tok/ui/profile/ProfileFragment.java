package com.sszabo.life_tok.ui.profile;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.StorageReference;
import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.adapter.ProfileAdapter;
import com.sszabo.life_tok.databinding.FragmentProfileBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProfileFragment extends Fragment {

    private static final String TAG = ProfileFragment.class.getSimpleName();

    private ProfileViewModel profileViewModel;
    private FragmentProfileBinding binding;

    private ImageView profPic;
    private TextView txtUsername;
    private TextView txtNumFollowers;
    private TextView txtNumFollowing;

    private ArrayList<Event> eventsList;
    private RecyclerView rvProfile;

    // TODO create adapter for recyclerview and
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        profileViewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        eventsList = new ArrayList<>();

        rvProfile = binding.recyclerViewProfile;
        profPic = binding.imageViewProfilePic;
        txtUsername = binding.txtUsernameProfile;
        txtNumFollowers = binding.txtNumFollowers;
        txtNumFollowing = binding.txtNumFollowing;

        setHasOptionsMenu(true);

        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_nav_profile_to_nav_settings);
                return true;
            case R.id.action_logout:
                FirebaseUtil.getAuth().signOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        final long TEN_MEGABYTE = 10 * 1024 * 1024;
        User user = MainViewModel.getCurrentUser();
        StorageReference ref = FirebaseUtil.getStorage().getReferenceFromUrl(user.getPictureUrl());
        ref.getBytes(TEN_MEGABYTE).addOnCompleteListener(new OnCompleteListener<byte[]>() {
            @Override
            public void onComplete(@NonNull Task<byte[]> task) {
                if (task.isSuccessful()) {
                    File temp = null;
                    try {
                        // write to temporary file
                        File outputDir = getContext().getCacheDir();
                        temp = File.createTempFile(user.getId(), ".jpg", outputDir);
                        FileOutputStream fos = new FileOutputStream(temp);
                        fos.write(task.getResult());
                        temp.deleteOnExit();
                    } catch (IOException | NullPointerException e) {
                        e.printStackTrace();
                    }

                    if (temp != null) {
                        // display picture
                        profPic.setImageURI(Uri.fromFile(temp));
                    } else {
                        Toast.makeText(getContext(), "Temporary profile picture save failed", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Could not get profile picture", Toast.LENGTH_SHORT).show();
                }
            }
        });

        txtUsername.setText(MainViewModel.getCurrentUser().getUsername());
        txtNumFollowers.setText(Integer.toString(user.getFollowers().size()));
        txtNumFollowing.setText(Integer.toString(user.getFollowing().size()));
        getMyEvents();
    }

    // get user's events
    private void getMyEvents() {
        eventsList.clear();

        ArrayList<Task<QuerySnapshot>> eventTasks = new ArrayList<>();
        User user = MainViewModel.getCurrentUser();

        eventTasks.add(FirebaseUtil.getFirestore()
                .collection("publicEvents")
                .whereEqualTo("userId", user.getId())
                .get());

        eventTasks.add(FirebaseUtil.getFirestore()
                .collection("users")
                .document(user.getId())
                .collection("events")
                .get());

        Tasks.whenAllComplete(eventTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
            @Override
            public void onComplete(@NonNull Task<List<Task<?>>> task) {
                if (task.isSuccessful()) {
                    for (Task<?> t : task.getResult()) {
                        eventsList.addAll(((QuerySnapshot) t.getResult()).toObjects(Event.class));
                    }
                    rvProfile.setAdapter(new ProfileAdapter(eventsList));
                    rvProfile.setLayoutManager(new LinearLayoutManager(getContext()));
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
