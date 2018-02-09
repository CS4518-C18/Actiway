package com.cs4518.poseidon.myapplication;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * @author jiuchuan
 * @version Feb 8, 2018
 */

public class GeofenceManager {

    // Constants
    // public static final String TAG = Geofencing.class.getSimpleName();
    private static final float GEOFENCE_RADIUS = 50;
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000;

    ArrayList<Geofence> mGeofenceList;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;

    public GeofenceManager(Context context){
        mGeofencingClient = LocationServices.getGeofencingClient(context);
        mContext = context;
    }

    public void addGeofencing () {
        try {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent());
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void removeGeofencing () {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    public void updateGeofencesList(){
        mGeofenceList = new ArrayList<>();
        String placeUID = "Fuller Lab";
        double placeLat = 42.274851;
        double placeLng = -71.806665;
        // Build a Geofence object
        Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();
        // Add it to the list
        mGeofenceList.add(geofence);
    }

    /***
     * Creates a GeofencingRequest object using the mGeofenceList ArrayList of Geofences
     * Used by {@code #registerGeofences}
     *
     * @return the GeofencingRequest object
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceTransitionsIntentService.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
}
