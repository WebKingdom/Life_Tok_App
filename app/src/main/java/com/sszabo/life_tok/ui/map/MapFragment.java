package com.sszabo.life_tok.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.QuerySnapshot;
import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentMapBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.util.FirebaseUtil;
import com.sszabo.life_tok.util.Resources;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final float DEFAULT_ZOOM = 12;
    private static final float CLOSE_ZOOM = 16;

    private MapViewModel mapViewModel;
    private FragmentMapBinding binding;

    private FloatingActionButton btnRefreshMap;
    private SearchView searchView;
    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;

    private ActivityResultLauncher<String[]> activityResultLauncher;

    private ArrayList<Event> eventsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        setHasOptionsMenu(true);

        if (savedInstanceState != null) {
            // TODO? load data from save
        }

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

        eventsList = new ArrayList<>();
        btnRefreshMap = binding.floatingBtnRefreshMap;
        mMapView = binding.mapView;
        mMapView.onCreate(savedInstanceState);

        setListeners();

        Log.d(TAG, "onCreateView: Created view");
        return root;
    }

    private void setListeners() {
        btnRefreshMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getPublicAndPrivateEvents();
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.default_menu, menu);

        MenuItem menuSearchItem = menu.findItem(R.id.item_action_search);
        searchView = (SearchView) menuSearchItem.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (query.isEmpty() || query.length() < 2) {
                    Toast.makeText(getContext(), "Must search more than 1 letters", Toast.LENGTH_SHORT).show();
                    return true;
                }

                // search events by name, description, or location
                ArrayList<Event> searchEvents = new ArrayList<>();
                for (Event event : eventsList) {
                    if (event.searchContains(query.toLowerCase())) {
                        searchEvents.add(event);
                    }
                }

                if (searchEvents.isEmpty()) {
                    Toast.makeText(getContext(), "No events found", Toast.LENGTH_SHORT).show();
                    return true;
                }
                // clear markers and display only searched ones
                mGoogleMap.clear();
                displayEvents(searchEvents);
                LatLng curCoord = new LatLng(searchEvents.get(0).getGeoPoint().getLatitude(),
                        searchEvents.get(0).getGeoPoint().getLongitude());
                mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curCoord, DEFAULT_ZOOM));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        // TODO? save state
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this.getContext());
        mMapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();

        try {
            MapsInitializer.initialize(this.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mMapView.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mMapView.onDestroy();
        binding = null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mGoogleMap = googleMap;

        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION};

        activityResultLauncher.launch(permissions);

        for (String permission : permissions) {
            if (ActivityCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
                // permission not granted, return
                Toast.makeText(getContext(), "Enable permissions for functionality ", Toast.LENGTH_SHORT).show();
                onPause();
                onStop();
                return;
            }
        }

        mGoogleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.mapstyle_life_tok));

        // get and display event locations
        getPublicAndPrivateEvents();

        if (getArguments() != null) {
            // Navigated from home page or event view to here with event selected
            Event curEvent = (Event) getArguments().getSerializable(Resources.KEY_EVENT);
            LatLng curCoor = new LatLng(curEvent.getGeoPoint().getLatitude(), curEvent.getGeoPoint().getLongitude());
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curCoor, CLOSE_ZOOM));
        } else {
            fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
                @Override
                public void onComplete(@NonNull Task<Location> task) {
                    if (task.isSuccessful()) {
                        Location curLoc = task.getResult();
                        LatLng curCoord = new LatLng(curLoc.getLatitude(), curLoc.getLongitude());
                        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curCoord, DEFAULT_ZOOM));
                    } else {
                        Toast.makeText(getContext(), "Could not get location", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        mGoogleMap.setMyLocationEnabled(true);

        mGoogleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(@NonNull Marker marker) {
                navToEventViewFragment((int) marker.getZIndex());
            }
        });
    }

    private void navToEventViewFragment(int index) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(Resources.KEY_EVENT, eventsList.get(index));

        NavHostFragment.findNavController(MapFragment.this)
                .navigate(R.id.action_nav_map_to_nav_event_view, bundle);
    }

    /**
     * Gets and marks all public and following user events on the map.
     * Clears all markers and the eventList before setting any new markers
     */
    private void getPublicAndPrivateEvents() {
        mGoogleMap.clear();
        eventsList.clear();
        User curUser = MainViewModel.getCurrentUser();

        // get all public events
        FirebaseUtil.getFirestore()
                .collection("publicEvents")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            // add marker for event on map
                            eventsList.addAll(task.getResult().toObjects(Event.class));

                            // get all private events of followers
                            ArrayList<Task<QuerySnapshot>> privateEventTasks = new ArrayList<>();
                            for (String id : curUser.getFollowing()) {
                                privateEventTasks.add(FirebaseUtil.getFirestore()
                                        .collection("users")
                                        .document(id)
                                        .collection("events")
                                        .get());
                            }

                            // include private events of current user (yourself)
                            privateEventTasks.add(FirebaseUtil.getFirestore()
                                    .collection("users")
                                    .document(curUser.getId())
                                    .collection("events")
                                    .get());

                            Tasks.whenAllComplete(privateEventTasks).addOnCompleteListener(new OnCompleteListener<List<Task<?>>>() {
                                @Override
                                public void onComplete(@NonNull Task<List<Task<?>>> task) {
                                    if (task.isSuccessful()) {
                                        for (Task<?> t : task.getResult()) {
                                            eventsList.addAll( ((QuerySnapshot) t.getResult()).toObjects(Event.class) );
                                        }
                                        displayEvents(eventsList);
                                    } else {
                                        Toast.makeText(getContext(), "Error finding following event", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(getContext(), "Error finding public events", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    /**
     * Shows the list of events as markers on the map
     *
     * @param list of events to show on map
     */
    private void displayEvents(ArrayList<Event> list) {
        for (int i = 0; i < list.size(); i++) {
            Event e = list.get(i);
            String owner = "@" + e.getUsername();
            LatLng pos = new LatLng(e.getGeoPoint().getLatitude(), e.getGeoPoint().getLongitude());
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(e.getName())
                    .alpha(0.7f)
                    .zIndex(i)
                    .snippet(owner));
        }
    }
}
