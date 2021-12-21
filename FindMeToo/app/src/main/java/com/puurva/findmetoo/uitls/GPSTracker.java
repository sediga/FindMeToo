package com.puurva.findmetoo.uitls;

import android.content.Context;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

/**
 * Create this Class from tutorial :
 * http://www.androidhive.info/2012/07/android-gps-location-manager-tutorial
 * <p>
 * For Geocoder read this : http://stackoverflow.com/questions/472313/android-reverse-geocoding-getfromlocation
 */

public class GPSTracker  {

    private FusedLocationProviderClient fusedLocationClient;
    private Context context;
    public GPSTracker(Context context){
        this.context = context;
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }
    public void GetLastLocation() {
//        fusedLocationClient.getLastLocation()
//                .addOnSuccessListener((Activity) context, new OnSuccessListener<Location>() {
//                    @Override
//                    public void onSuccess(Location location) {
//                        // Got last known location. In some rare situations this can be null.
//                        if (location != null) {
//                        }
//                    }
//                });
    }
}
