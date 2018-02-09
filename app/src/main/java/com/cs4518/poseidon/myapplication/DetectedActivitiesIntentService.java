package com.cs4518.poseidon.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

/**
 * @author Harry Liu
 * @version Feb 9, 2018
 */

public class DetectedActivitiesIntentService extends IntentService {
    public DetectedActivitiesIntentService() {
        super("DetectedActivitiesIS");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        ActivityRecognitionResult recognitionResult = ActivityRecognitionResult.extractResult(intent);
        ArrayList<DetectedActivity> detectedActivities = (ArrayList) recognitionResult.getProbableActivities();
        Collections.sort(detectedActivities, new Comparator<DetectedActivity>() {
            @Override
            public int compare(DetectedActivity detectedActivity1, DetectedActivity detectedActivity2) {
                return detectedActivity2.getConfidence() - detectedActivity1.getConfidence();
            }
        });

        int activity = detectedActivities.get(0).getType();
        Utilities.updateActivity(this, activity);

    }
}
