package com.sszabo.life_tok.ui.map;

import android.location.Geocoder;
import android.location.Location;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;

/**
 * View Model class for the Map Fragment.
 */
public class MapViewModel extends ViewModel {
    private static final String TAG = MapViewModel.class.getSimpleName();

    /**
     * Constructor
     */
    public MapViewModel() {
    }
}
