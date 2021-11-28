package com.sszabo.life_tok.ui.map;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.sszabo.life_tok.MainViewModel;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentMapBinding;
import com.sszabo.life_tok.model.Event;
import com.sszabo.life_tok.model.User;
import com.sszabo.life_tok.ui.login.RegisterActivity;
import com.sszabo.life_tok.util.FirebaseUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import kotlinx.coroutines.DefaultExecutorKt;

public class MapFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final float DEFAULT_ZOOM = 10;

    private MapViewModel mapViewModel;
    private FragmentMapBinding binding;

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Geocoder geocoder;

    private ActivityResultLauncher<String[]> activityResultLauncher;

    private ArrayList<Event> eventsList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);

        binding = FragmentMapBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SearchView searchView = root.findViewById(R.id.searchViewMap);
        mapViewModel.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                searchView.getQuery();

            }
        });

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
                            Toast.makeText(getContext(), "Must enable permissions for functionality", Toast.LENGTH_LONG).show();
                            onPause();
                            onStop();
                        }
                    }
                });

        eventsList = new ArrayList<>();
        mMapView = root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        Log.d(TAG, "onCreateView: Created view");
        return root;
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
        geocoder = new Geocoder(this.getContext());
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
                Toast.makeText(getContext(), "Must enable permissions for functionality ", Toast.LENGTH_LONG).show();
                onPause();
                onStop();
                return;
            }
        }

        // get and display event locations
        mGoogleMap.clear();
        getPublicAndFollowingEvents();

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

        mGoogleMap.setMyLocationEnabled(true);
    }

    private void getPublicAndFollowingEvents() {
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
                            displayEvents(eventsList);
                        } else {
                            Toast.makeText(getContext(), "Error finding public events", Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        // get all private events of followers
        ArrayList<Event> tempEventList = new ArrayList<>();
        for (String id : curUser.getFollowing()) {
            FirebaseUtil.getFirestore()
                    .collection("users")
                    .document(id)
                    .collection("events")
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                // add marker for event on map
                                tempEventList.addAll(task.getResult().toObjects(Event.class));
                                eventsList.addAll(tempEventList);
                                displayEvents(tempEventList);
                            } else {
                                Toast.makeText(getContext(), "Error finding following event", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void displayEvents(ArrayList<Event> list) {
        for (Event event : list) {
            LatLng pos = new LatLng(event.getGeoPoint().getLatitude(), event.getGeoPoint().getLongitude());
            mGoogleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(event.getName())
                    .snippet(event.getDescription()));
        }
    }
}
