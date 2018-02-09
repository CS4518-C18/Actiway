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
    private static final long GEOFENCE_TIMEOUT = Geofence.NEVER_EXPIRE;
    public static final Geofence fullerGeofence = new Geofence.Builder()
            .setRequestId("Fuller Lab")
            .setExpirationDuration(GEOFENCE_TIMEOUT)
            .setCircularRegion(42.274851, -71.806665, GEOFENCE_RADIUS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
            .build();
    public static final Geofence libraryGeofence = new Geofence.Builder()
            .setRequestId("Library")
            .setExpirationDuration(GEOFENCE_TIMEOUT)
            .setCircularRegion(42.274239, -71.806646, GEOFENCE_RADIUS)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
            .build();;

    private ArrayList<Geofence> mGeofenceList;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mGeofencePendingIntent;
    private Context mContext;

    public GeofenceManager(Context context){
        mContext = context;
        mGeofencingClient = LocationServices.getGeofencingClient(mContext);
    }

    public void addGeofencing () {
        try {
            mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("add geofences success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("add geofences fail");
                    }
                });
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void removeGeofencing () {
        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).
                addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        System.out.println("remove geofences success");
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        System.out.println("remove geofences fail");
                    }
                });
    }

    public void intializeGeofencesList(){
        mGeofenceList = new ArrayList<>();
        // Build a geofence
        // Add it to the list
        mGeofenceList.add(fullerGeofence);
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
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getService(mContext, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }
}
