package com.cs4518.poseidon.myapplication;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * @author Poseidon
 * @author Harry Liu
 * @version Feb 8, 2018
 */

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, SensorEventListener {
    // step counter
    private Boolean running = true;
    private SensorManager sensorManager;
    private Boolean forTheFirstTime = true;
    private Boolean finishedSixSteps = false;
    private float initialStepInGeofence = 0;

    // geofence
    private GeofenceManager mGeofenceManager;

    // numEnteredGeofence
    private int numEnteredFullerGeofence = 0;
    private int numEnteredLibraryGeofence = 0;
    private final String NUM_ENTERED_GEOFENCE = "SHARED_PREFERENCE_NUM_ENTERED_GEOFENCE";
    private final String NUM_ENTERED_FULLER_GEOFENCE = "NUM_ENTERED_FULLER_GEOFENCE";
    private final String NUM_ENTERED_LIBRARY_GEOFENCE = "NUM_ENTERED_LIBRARY_GEOFENCE";
    SharedPreferences numEnteredSharedPref;

    private TextView mTextViewFullerLab;
    private TextView mTextViewLibrary;
    private MapView mMapView;
    private ImageView mActivityImageView;
    private TextView mTextViewActivity;

    private int fuller_lab_count;
    private int library_count;
    private Activity currentActivity;
    private boolean mLocationPermissionGranted;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private GoogleMap mMap;
    private Location mLastKnownLocation;

    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private final int DEFAULT_ZOOM = 18;

    private LocationRequest locationRequest;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextViewFullerLab = findViewById(R.id.text_view_fuller_lab);
        mTextViewLibrary = findViewById(R.id.text_view_library);

        updateGeoFenceCounts();

        mMapView = findViewById(R.id.map_view);
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        mActivityImageView = findViewById(R.id.image_view_activity);
        mTextViewActivity = findViewById(R.id.text_view_activity);

        currentActivity = Activity.STILL;

        updateActivity();

        // initialize geofence manager
        mGeofenceManager = new GeofenceManager(this);
        mGeofenceManager.intializeGeofencesList();
        mGeofenceManager.addGeofencing();

        // initialize counter sensor
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // load numEnteredGeofence shared pref
        numEnteredSharedPref = this.getSharedPreferences(
                NUM_ENTERED_GEOFENCE, Context.MODE_PRIVATE);
        numEnteredFullerGeofence = numEnteredSharedPref.getInt(NUM_ENTERED_FULLER_GEOFENCE, 0);
        numEnteredLibraryGeofence = numEnteredSharedPref.getInt(NUM_ENTERED_LIBRARY_GEOFENCE, 0);

        setNumEnteredGeofenceText();

//        // Assign views
//        imageView = findViewById(R.id.imageView2);
//        textView = findViewById(R.id.activity);
//        geofence1 =  findViewById(R.id.geofence1);
//        geofence2 = findViewById(R.id.geofence2);
//
//        mApiClient = new GoogleApiClient.Builder(this)
//                .addApi(ActivityRecognition.API)
//                .addConnectionCallbacks(this)
//                .addOnConnectionFailedListener(this)
//                .build();
//
//        mApiClient.connect();
//
//        // Manipulate Map
//        mapView =  findViewById(R.id.mapView);
//        mapView.onCreate(savedInstanceState);
//        mapView.getMapAsync(new OnMapReadyCallback() {
//            @Override
//            public void onMapReady(GoogleMap mMap) {
//                googleMap = mMap;
//                LatLng current = new LatLng(42, -71);
//                googleMap.addMarker(new MarkerOptions().position(current));
//                googleMap.moveCamera(CameraUpdateFactory.newLatLng(current));
//            }
//        });
//
//        mBroadcastFilter = new IntentFilter("PleaseWork");
//        mBroadcastFilter.addCategory("SampleCategory");
//        mBroadcastManager = LocalBroadcastManager.getInstance(this);
//        BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
//            @Override
//            public void onReceive(Context context, Intent intent) {
//                // When an Intent is received from the update listener IntentService,
//                // update the display.
//                uiUpdate(intent.getStringExtra("Activity"));
//            }
//        };
//
//        mBroadcastManager.registerReceiver(updateListReceiver, mBroadcastFilter);
        locationRequest = getLocationRequest();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
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
        mGeofenceManager.removeGeofencing();
        SharedPreferences.Editor editor = numEnteredSharedPref.edit();
        editor.putInt(NUM_ENTERED_FULLER_GEOFENCE, numEnteredFullerGeofence);
        editor.putInt(NUM_ENTERED_LIBRARY_GEOFENCE, numEnteredLibraryGeofence);
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
        final long UPDATE_INTERVAL = 1000L;
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
                        //System.out.println("lat: " + mLastKnownLocation.getLatitude());
                        //System.out.println("long: " + mLastKnownLocation.getLongitude());
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
        int activity = R.string.still;
        switch (currentActivity) {
            case STILL:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.still));
                activity = R.string.still;
                break;
            case WALKING:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.walking));
                activity = R.string.walking;
                break;
            case RUNNING:
                mActivityImageView.setImageDrawable(getDrawable(R.drawable.running));
                activity = R.string.running;
                break;
        }
        mTextViewActivity.setText(getString(R.string.current_activity, getString(activity)));
    }

    private void updateGeoFenceCounts() {
        mTextViewFullerLab.setText(getString(R.string.visit_to_fuller_lab, fuller_lab_count));
        mTextViewLibrary.setText(getString(R.string.visit_to_library, library_count));
    }

//
//    public void uiUpdate(String activityType) {
////        textView.setText(activityType);
////
////        Log.e("uiUpdate", activityType);
////        switch(activityType) {
////            case ActivityRecognizedService.WALKING:
////                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.walking));
////                break;
////            case ActivityRecognizedService.RUNNING:
////                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.running));
////                break;
////            case ActivityRecognizedService.STILL:
////                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.still));
////                break;
////
////        }
//    }
//
//    @Override
//    public void onConnected(@Nullable Bundle bundle) {
////        Intent intent = new Intent( this, ActivityRecognizedService.class );
////        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
////        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 10, pendingIntent ).setResultCallback(new ResultCallback<Status>() {
////            @Override
////            public void onResult(@NonNull Status status) {
////                Log.e("SUBCLASS", "Return from activity recognition");
////            }
////        });
//    }
//
//    @Override
//    public void onConnectionSuspended(int i) {
//
//    }
//
//    @Override
//    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
//
//    }
//
    @Override
    public void onSensorChanged(SensorEvent event) {
        if (running) {
            if (GeofenceTransitionsIntentService.inFullerGeofence) {
                enterGeofence(event, "fuller");
            } else if (GeofenceTransitionsIntentService.inLibraryGeofence) {
                enterGeofence(event, "library");
            } else {
                forTheFirstTime = true;
                finishedSixSteps = false;
            }
        }
    }

    private void enterGeofence (SensorEvent event, String geofenceName) {
        if (!finishedSixSteps) {
            if (forTheFirstTime) {
                if (geofenceName.equals("fuller")) {
                    System.out.println("entering fuller geofence...");
                } else if (geofenceName.equals("library")) {
                    System.out.println("entering library geofence...");
                } else {
                    return;
                }
                initialStepInGeofence = event.values[0];
                forTheFirstTime = false;
            } else {
                if (event.values[0] - initialStepInGeofence >= 6) {
                    if (geofenceName.equals("fuller")) {
                        numEnteredFullerGeofence++;
                        Toast.makeText(this,
                                "6 steps in fuller geofence",
                                Toast.LENGTH_SHORT).show();
                    } else if (geofenceName.equals("library")) {
                        numEnteredLibraryGeofence++;
                        Toast.makeText(this,
                                "6 steps in library geofence",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        return;
                    }
                    finishedSixSteps = true;
                    setNumEnteredGeofenceText();
                }
            }
        }
    }

    private void setNumEnteredGeofenceText () {
        String fullerText = "You visited Fuller lab for "
                + String.valueOf(numEnteredFullerGeofence)
                + " times";
        String libraryText = "You visited Library for "
                + String.valueOf(numEnteredLibraryGeofence)
                + " times";
        mTextViewFullerLab.setText(fullerText);
        mTextViewLibrary.setText(libraryText);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}


