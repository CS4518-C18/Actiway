package com.cs4518.poseidon.myapplication;


import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.DetectedActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

/**
 * @author Poseidon
 * @author Harry Liu
 * @version Feb 8, 2018
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener {

    private TextView mTextViewFullerLab;
    private TextView mTextViewLibrary;
    private MapView mMapView;
    private ImageView mActivityImageView;
    private TextView mTextViewActivity;

    private int fuller_lab_count;
    private int library_count;
    private int currentActivity;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location mLastKnownLocation;

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int DEFAULT_ZOOM = 18;

    private LocationRequest locationRequest;
    private ActivityRecognitionClient mActivityRecognitionClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fuller_lab_count = 0;
        library_count = 0;

        mTextViewFullerLab = findViewById(R.id.text_view_fuller_lab);
        mTextViewLibrary = findViewById(R.id.text_view_library);

        updateGeoFenceCounts();

        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mActivityImageView = findViewById(R.id.image_view_activity);
        mTextViewActivity = findViewById(R.id.text_view_activity);

        currentActivity = DetectedActivity.STILL;

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        updateActivity();

        locationRequest = getLocationRequest();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        requestActivityUpdate();
    }

    @Override
    protected void onStart() {
        mMapView.onStart();
        super.onStart();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        super.onResume();
    }

    @Override
    protected void onStop() {
        mMapView.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mMapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        mMapView.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onPause() {
        mMapView.onPause();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        mMapView.onLowMemory();
        super.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(this.getClass().getSimpleName(), "Map ready");
        updateDeviceLocation();
    }

    private void requestActivityUpdate() {
        final long DETECTION_INTERVAL_IN_MILLISECONDS = 500;

        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                getActivityDetectionPendingIntent());
    }

    private PendingIntent getActivityDetectionPendingIntent() {
        Intent intent = new Intent(this, DetectedActivitiesIntentService.class);
        return PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                    updateDeviceLocation();
                }
            }
        }
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private LocationRequest getLocationRequest() {
        final long UPDATE_INTERVAL = 0;
        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(UPDATE_INTERVAL);
        locationRequest.setFastestInterval(UPDATE_INTERVAL);
        return locationRequest;
    }

    private void updateDeviceLocation() {
        try {
            if (mLocationPermissionGranted) {
                mFusedLocationProviderClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        mLastKnownLocation = locationResult.getLastLocation();

                        if (mMap == null) return;

                        LatLng latLng = new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude());

                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM));
                        mMap.clear();
                        mMap.addMarker(new MarkerOptions()
                                .position(latLng));
                    }

                    @Override
                    public void onLocationAvailability(LocationAvailability locationAvailability) {
                        super.onLocationAvailability(locationAvailability);
                    }
                }, Looper.myLooper());
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    private void updateActivity() {
        int activity;
        switch (currentActivity) {
            case DetectedActivity.STILL:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.still));
                activity = R.string.still;
                break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.WALKING:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.walking));
                activity = R.string.walking;
                break;
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.RUNNING:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.running));
                activity = R.string.running;
                break;
            default:
                mActivityImageView.setImageDrawable(null);
                activity = R.string.unknown;

        }
        mTextViewActivity.setText(getString(R.string.current_activity, getString(activity)));
    }

    private void updateGeoFenceCounts() {
        mTextViewFullerLab.setText(getString(R.string.visit_to_fuller_lab, fuller_lab_count));
        mTextViewLibrary.setText(getString(R.string.visit_to_library, library_count));
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.current_activity_key))) {
            currentActivity = sharedPreferences.getInt(s, DetectedActivity.UNKNOWN);
            updateActivity();
        }
    }
}


