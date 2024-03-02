package com.example.calorieguard;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LocationUpdater {

    private static final String TAG = "LocationUpdater";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Context context;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationUpdater(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    void requestLocationUpdates(String userNode, TextView city) {
        // Check for location permissions
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Request last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Handle location updates
                            Log.d(TAG, "Last Known Location: " + location.toString());

                            // Update the location in Firebase
                            updateLocationInFirestore(userNode, location,city);
                        } else {
                            // If last known location is not available, request location updates
                            requestLocationUpdatesContinuously(userNode,city);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get last known location: " + e.getMessage());
                    });
        } else {
            // Request location permission
            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void requestLocationUpdatesContinuously(String userNode,TextView city) {
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds
        locationRequest.setPriority(LocationRequest.PRIORITY_LOW_POWER); // Adjust priority for coarse location

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Handle location updates
                    Log.d(TAG, "Location Update: " + location.toString());

                    // Update the location in Firebase
                    updateLocationInFirestore(userNode, location,city);
                }
            }
        }, Looper.getMainLooper());
    }


    private void updateLocationInFirestore(String userNode, Location location, TextView city) {
        getCityStateCountry(context, location.getLatitude(), location.getLongitude(), city);

        Map<String, Object> locationData = new HashMap<>();
        locationData.put("Latitude", location.getLatitude());
        locationData.put("Longitude", location.getLongitude());
        locationData.put("Altitude", location.getAltitude());
        locationData.put("Accuracy", location.getAccuracy());

        db.collection("Users")
                .document(userNode)
                .collection("Location")
                .document("During Sign Up")
                .set(locationData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "Location updated successfully in Firestore!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Location Error", e.toString());
                    }
                });
    }

    public void getCityStateCountry(Context context, double LATITUDE, double LONGITUDE, TextView city) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        String result = "";

        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);

                String City = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();

                result = City + ", " + state + ", " + country;
                city.setText(result);
            }
        } catch (IOException e) {
            Log.d("Location Error",e.toString());
            e.printStackTrace();
        }

    }
}
