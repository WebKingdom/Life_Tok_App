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
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentMapBinding;

import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final float DEFAULT_ZOOM = 10;

    private MapViewModel mapViewModel;
    private FragmentMapBinding binding;

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Geocoder geocoder;

    private ActivityResultLauncher<String[]> activityResultLauncher;

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

        currentLocation = null;
        mMapView = root.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        // displays maps immediately
        mMapView.onResume();

        try {
            MapsInitializer.initialize(this.getContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(this);

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

        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                // TODO
                if (location != null) {
                    currentLocation = location;
                    LatLng curCoord = new LatLng(location.getLatitude(), location.getLongitude());

                    mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curCoord, DEFAULT_ZOOM));
                    mGoogleMap.clear();
                    mGoogleMap.addMarker(new MarkerOptions().position(curCoord).title("Current Location"));
                } else {
                    Log.d(TAG, "onMapReady: Could not get current location.");
                    Toast.makeText(MapFragment.this.getContext(), "Could not get current location.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mGoogleMap.setMyLocationEnabled(true);
    }

    private void getFollowingEventLocations() {
        // TODO mark all events on map for people you follow
    }
}
