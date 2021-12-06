package com.sszabo.life_tok.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.adapter.FeedAdapter;
import com.sszabo.life_tok.databinding.FragmentHomeBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.ArrayList;

public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private FragmentHomeBinding binding;
    private SearchView searchView;
    private ViewPager2 feedViewPager;

    private ArrayList<Event> eventsList;
    private FeedAdapter feedAdapter;
    private TextView txtFeedMessage;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        eventsList = new ArrayList<>();
        feedViewPager = binding.feedViewPager;
        txtFeedMessage = binding.txtFeedMessage;

        if (MainViewModel.getCurrentUser() == null) {
            String uid = FirebaseUtil.getAuth().getCurrentUser().getUid();
            FirebaseUtil.getFirestore()
                    .collection("users")
                    .document(uid)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                MainViewModel.setCurrentUser(task.getResult().toObject(User.class));
                                getFollowingUserEvents((ArrayList<String>) MainViewModel.getCurrentUser().getFollowing());
                            } else {
                                Toast.makeText(getContext(), "Could not get app user", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        } else {
            getFollowingUserEvents((ArrayList<String>) MainViewModel.getCurrentUser().getFollowing());
        }

        setHasOptionsMenu(true);

        return root;
    }

    private void getFollowingUserEvents(ArrayList<String> listUIDs) {
        // TODO cache events or add snapshot listeners so we don't always have to restart query
        eventsList.clear();

        for (String id : listUIDs) {
            FirebaseUtil.getFirestore()
                    .collection("users")
                    .document(id)
                    .collection("events")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                eventsList.addAll(task.getResult().toObjects(Event.class));

                                // get public events for the people you follow as well
                                FirebaseUtil.getFirestore()
                                        .collection("publicEvents")
                                        .whereEqualTo("userId", id)
                                        .get()
                                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                            @Override
                                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                if (task.isSuccessful()) {
                                                    // complete with all requests, set up ViewPager
                                                    eventsList.addAll(task.getResult().toObjects(Event.class));
                                                    feedAdapter = new FeedAdapter(eventsList, initGlide());
                                                    feedViewPager.setAdapter(feedAdapter);
                                                } else {
                                                    Toast.makeText(getContext(), "Could not get events for user: " + id, Toast.LENGTH_SHORT).show();
                                                }

                                                // set up visibility
                                                if (eventsList.isEmpty()) {
                                                    txtFeedMessage.setVisibility(View.VISIBLE);
                                                } else {
                                                    txtFeedMessage.setVisibility(View.INVISIBLE);
                                                }
                                            }
                                        });
                            } else {
                                Toast.makeText(getContext(), "Could not get events for user: " + id, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.default_menu, menu);

        MenuItem menuSearchItem = menu.findItem(R.id.item_action_search);
        searchView = (SearchView) menuSearchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // TODO search users to follow
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    private RequestManager initGlide() {
        RequestOptions options = new RequestOptions()
                .placeholder(R.drawable.ic_launcher_background)
                .error(R.drawable.ic_launcher_background);

        return Glide.with(this).setDefaultRequestOptions(options);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}