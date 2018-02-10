package com.cs4518.poseidon.myapplication;

import android.content.Context;
import android.preference.PreferenceManager;

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
}
