package com.cs4518.poseidon.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static android.content.ContentValues.TAG;

/**
 * Created by haofanzhang on 2/9/18.
 */

public class GeofenceTransitionsIntentService extends IntentService {
    public static Boolean inFullerGeofence = false;
    public static Boolean inLibraryGeofence = false;

    public GeofenceTransitionsIntentService() {
        super("GeofenceTransitionsIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            System.out.println("intent error");
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Enter geofence
            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            System.out.println("intent in geofence");
            if (triggeringGeofences.contains(GeofenceManager.fullerGeofence)) {
                inFullerGeofence = true;
            } else if (triggeringGeofences.contains(GeofenceManager.libraryGeofence)) {
                inLibraryGeofence = true;
            }
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
            // Exit geofence
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();
            System.out.println("intent out geofence");
            if (triggeringGeofences.contains(GeofenceManager.fullerGeofence)) {
                inFullerGeofence = false;
            } else if (triggeringGeofences.contains(GeofenceManager.libraryGeofence)) {
                inLibraryGeofence = false;
            }
        } else {
            // Log the error.
        }
    }

}
