package com.cs4518.poseidon.myapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
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
import android.widget.Toast;

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

import static com.cs4518.poseidon.myapplication.Utilities.formatTime;

/**
 * @author Haofan
 * @author Poseidon
 * @author Harry Liu
 * @version Feb 8, 2018
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SharedPreferences.OnSharedPreferenceChangeListener, SensorEventListener {
    // step counter
    private Boolean running = true;
    private SensorManager sensorManager;

    // geofence
    private GeofenceManager mGeofenceManager;

    // numEnteredGeofence
    private final String NUM_ENTERED_GEOFENCE_PREFERENCE = "SHARED_PREFERENCE_NUM_ENTERED_GEOFENCE";
    private final String NUM_ENTERED_FULLER_GEOFENCE = "NUM_ENTERED_FULLER_GEOFENCE";
    private final String NUM_ENTERED_LIBRARY_GEOFENCE = "NUM_ENTERED_LIBRARY_GEOFENCE";
    SharedPreferences numEnteredSharedPref;
    private CustomGeofence fullerGeofence = new CustomGeofence(
            "fuller lab",
            0,
            NUM_ENTERED_FULLER_GEOFENCE);
    private CustomGeofence libraryGeofence = new CustomGeofence(
            "library",
            0,
            NUM_ENTERED_LIBRARY_GEOFENCE);

    private TextView mTextViewFullerLab;
    private TextView mTextViewLibrary;
    private MapView mMapView;
    private ImageView mActivityImageView;
    private TextView mTextViewActivity;

    private int currentActivity;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location mLastKnownLocation;

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int DEFAULT_ZOOM = 18;

    private LocationRequest locationRequest;
    private ActivityRecognitionClient mActivityRecognitionClient;

    private long activityUpdatedAt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewFullerLab = findViewById(R.id.text_view_fuller_lab);
        mTextViewLibrary = findViewById(R.id.text_view_library);

        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mActivityImageView = findViewById(R.id.image_view_activity);
        mTextViewActivity = findViewById(R.id.text_view_activity);

        currentActivity = DetectedActivity.STILL;

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        updateActivity(DetectedActivity.UNKNOWN);

        // initialize counter sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // load numEnteredGeofence shared pref
        numEnteredSharedPref = this.getSharedPreferences(
                NUM_ENTERED_GEOFENCE_PREFERENCE, Context.MODE_PRIVATE);

        fullerGeofence.numEnteredGeofence = numEnteredSharedPref.getInt(fullerGeofence.SHARED_PREF_KEY, 0);
        libraryGeofence.numEnteredGeofence = numEnteredSharedPref.getInt(libraryGeofence.SHARED_PREF_KEY, 0);
        setNumEnteredGeofenceText();

        locationRequest = getLocationRequest();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();

        mActivityRecognitionClient = new ActivityRecognitionClient(this);
        requestActivityUpdate();

        // initialize geofence manager
        if (mLocationPermissionGranted) {
            mGeofenceManager = new GeofenceManager(this);
            mGeofenceManager.intializeGeofencesList();
            mGeofenceManager.addGeofencing();
        }
    }


    @Override
    protected void onStart() {
        mMapView.onStart();
        super.onStart();
    }

    @Override
    protected void onResume() {
        mMapView.onResume();
        running = true;
        Sensor countSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (countSensor != null) {
            sensorManager.registerListener(this, countSensor, SensorManager.SENSOR_DELAY_UI);
        } else {
            Toast.makeText(this, "sensor not found", Toast.LENGTH_SHORT).show();
        }
        super.onResume();
    }

    @Override
    protected void onStop() {
        mMapView.onStop();
        if (mGeofenceManager != null) mGeofenceManager.removeGeofencing();
        SharedPreferences.Editor editor = numEnteredSharedPref.edit();
        editor.putInt(fullerGeofence.SHARED_PREF_KEY, fullerGeofence.numEnteredGeofence);
        editor.putInt(libraryGeofence.SHARED_PREF_KEY, libraryGeofence.numEnteredGeofence);
        editor.apply();
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
        running = false;
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
                    mGeofenceManager = new GeofenceManager(this);
                    mGeofenceManager.intializeGeofencesList();
                    mGeofenceManager.addGeofencing();
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

    private void updateActivity(int lastActivity) {

        if (activityUpdatedAt == 0)
            activityUpdatedAt = System.currentTimeMillis();
        else {
            long currentTime = System.currentTimeMillis();
            long timeElapsed = currentTime - activityUpdatedAt;
            activityUpdatedAt = currentTime;

            String formattedTime = formatTime(timeElapsed);
            String lastActivityName = Utilities.toString(this, lastActivity);
            String message = getString(R.string.activity_time_elapse, lastActivityName, formattedTime);
            Toast.makeText(this, message, Toast.LENGTH_LONG)
                    .show();
        }

        switch (currentActivity) {
            case DetectedActivity.STILL:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.still));
                break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.WALKING:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.walking));
                break;
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.RUNNING:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.running));
                break;
            default:
                mActivityImageView.setImageDrawable(null);

        }

        String activityString = Utilities.toString(this, currentActivity);
        mTextViewActivity.setText(getString(R.string.current_activity, activityString));
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running) {
            if (GeofenceTransitionsIntentService.inFullerGeofence) {
                enterGeofence(fullerGeofence, event);
            } else {
                if (fullerGeofence.outForTheFirstTime) {
                    fullerGeofence.inForTheFirstTime = true;
                    fullerGeofence.finishedSixSteps = false;
                    fullerGeofence.outForTheFirstTime = false;
                }
            }
            if (GeofenceTransitionsIntentService.inLibraryGeofence) {
                enterGeofence(libraryGeofence, event);
            } else {
                if (libraryGeofence.outForTheFirstTime) {
                    libraryGeofence.inForTheFirstTime = true;
                    libraryGeofence.finishedSixSteps = false;
                    libraryGeofence.outForTheFirstTime = false;
                }
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(getString(R.string.current_activity_key))) {
            int lastActivity = currentActivity;
            currentActivity = sharedPreferences.getInt(s, DetectedActivity.UNKNOWN);
            updateActivity(lastActivity);
        }
    }

    private void enterGeofence(CustomGeofence cGeofence, SensorEvent event) {
        if (!cGeofence.finishedSixSteps) {
            if (cGeofence.inForTheFirstTime) {
                String enterGeofence = "entering " + cGeofence.name + " geofence";
                Toast.makeText(this,
                        enterGeofence,
                        Toast.LENGTH_SHORT).show();
                cGeofence.initialStepInGeofence = event.values[0];
                cGeofence.inForTheFirstTime = false;
                cGeofence.outForTheFirstTime = true;
            } else {
                if (event.values[0] - cGeofence.initialStepInGeofence >= 6) {
                    cGeofence.numEnteredGeofence++;
                    String sixStepGeofence = "6 steps in " + cGeofence.name + " geofence";
                    Toast.makeText(this,
                            sixStepGeofence,
                            Toast.LENGTH_SHORT).show();
                    System.out.println(sixStepGeofence);
                    cGeofence.finishedSixSteps = true;
                    setNumEnteredGeofenceText();
                }
            }
        }
    }

    private void setNumEnteredGeofenceText() {
        String fullerText = "You visited Fuller lab for "
                + String.valueOf(fullerGeofence.numEnteredGeofence)
                + " times";
        String libraryText = "You visited Library for "
                + String.valueOf(libraryGeofence.numEnteredGeofence)
                + " times";
        mTextViewFullerLab.setText(fullerText);
        mTextViewLibrary.setText(libraryText);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}


