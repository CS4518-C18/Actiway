package com.cs4518.poseidon.myapplication;

import android.content.Context;
import android.preference.PreferenceManager;

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
}
