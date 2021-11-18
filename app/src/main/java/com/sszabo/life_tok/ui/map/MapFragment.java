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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.sszabo.life_tok.R;
import com.sszabo.life_tok.databinding.FragmentMapBinding;

import java.util.concurrent.ExecutionException;

public class MapFragment extends Fragment implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = MapFragment.class.getSimpleName();

    private static final float DEFAULT_ZOOM = 10;
    private static final String MAP_VIEW = "MAP_VIEW";
    private static final String GOOGLE_MAP = "GOOGLE_MAP";
    private static final String FUSED_LOCATION = "FUSED_LOCATION";
    private static final String GEOCODER = "GEOCODER";

    private MapViewModel mapViewModel;
    private FragmentMapBinding binding;

    private MapView mMapView;
    private GoogleMap mGoogleMap;
    private Location currentLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Geocoder geocoder;

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
        super.onSaveInstanceState(outState);
        // TODO? save state
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

        if (ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "onMapReady: Location permission denied/unavailable, requesting access.");
            ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
        }

        mGoogleMap.setMyLocationEnabled(true);

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
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case 1: {
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    if ((ContextCompat.checkSelfPermission(MapFragment.this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                            (ContextCompat.checkSelfPermission(MapFragment.this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {

                        Toast.makeText(this.getContext(), "Permission Granted", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onRequestPermissionsResult: Permission granted");
                    }
                } else {
                    Toast.makeText(this.getContext(), "Permission Denied", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onRequestPermissionsResult: Permission denied");
                }
            }
        }
    }
}
