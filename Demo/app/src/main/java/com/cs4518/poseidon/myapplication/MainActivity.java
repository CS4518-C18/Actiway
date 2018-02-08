package com.cs4518.poseidon.myapplication;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.ActivityRecognition;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Poseidon on 2/6/18.
 */

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public GoogleApiClient mApiClient;
    private MapView mapView;
    private TextView textView;
    private ImageView imageView;
    private GoogleMap googleMap;
    IntentFilter mBroadcastFilter;
    private LocalBroadcastManager mBroadcastManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Assign views
        imageView = (ImageView) findViewById(R.id.imageView2);
        textView = (TextView) findViewById(R.id.activity);

        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mApiClient.connect();




        // Manipulate Map
        mapView = (MapView) findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                LatLng current = new LatLng(42, -71);
                googleMap.addMarker(new MarkerOptions().position(current));
                googleMap.moveCamera(CameraUpdateFactory.newLatLng(current));
            }
        });

        mBroadcastFilter = new IntentFilter("PleaseWork");
        mBroadcastFilter.addCategory("SampleCategory");
        mBroadcastManager = LocalBroadcastManager.getInstance(this);
        BroadcastReceiver updateListReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // When an Intent is received from the update listener IntentService,
                // update the display.
                uiUpdate(intent.getStringExtra("Activity"));
            }
        };

        mBroadcastManager.registerReceiver(updateListReceiver, mBroadcastFilter);


    }

    public void uiUpdate(String activityType) {
        textView.setText(activityType);


        Log.e("uiUpdate", activityType);
        switch(activityType) {
            case ActivityRecognizedService.WALKING:

                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.walking));
                break;
            case ActivityRecognizedService.RUNNING:

                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.running));
                break;
            case ActivityRecognizedService.STILL:
                imageView.setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.still));
                break;

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent( this, ActivityRecognizedService.class );
        PendingIntent pendingIntent = PendingIntent.getService( this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates( mApiClient, 10, pendingIntent ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.e("SUBCLASS", "Return from activity recognition");
            }
        });
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

