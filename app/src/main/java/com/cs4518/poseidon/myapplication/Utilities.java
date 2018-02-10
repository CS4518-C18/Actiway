package com.cs4518.poseidon.myapplication;

import android.content.Context;
import android.preference.PreferenceManager;

import com.google.android.gms.location.DetectedActivity;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * @author Harry Liu
 * @version Feb 9, 2018
 */

public class Utilities {

    static void updateActivity(Context context, int activity) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putInt(context.getString(R.string.current_activity_key), activity)
                .apply();
    }

    static String formatTime(long milliseconds) {
        return String.format(Locale.getDefault(), "%d min, %d seconds",
                TimeUnit.MILLISECONDS.toMinutes(milliseconds),
                TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(milliseconds))
        );
    }

    static String toString(Context context, int activity) {
        int activityID;
        switch (activity) {
            case DetectedActivity.STILL:
                activityID = R.string.still;
                break;
            case DetectedActivity.ON_FOOT:
            case DetectedActivity.WALKING:
                activityID = R.string.walking;
                break;
            case DetectedActivity.ON_BICYCLE:
            case DetectedActivity.RUNNING:
                activityID = R.string.running;
                break;
            default:
                activityID = R.string.unknown;

        }

        return context.getString(activityID);
    }
}
