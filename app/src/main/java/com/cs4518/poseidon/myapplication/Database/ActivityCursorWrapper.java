package com.cs4518.poseidon.myapplication.Database;

import android.database.Cursor;
import android.database.CursorWrapper;

import java.sql.Timestamp;
import java.util.UUID;

import com.cs4518.poseidon.myapplication.Database.ActivityDbSchema.ActivityTable;
import com.cs4518.poseidon.myapplication.model.Activity;

/**
 * Created by Poseidon on 2/6/18.
 */

public class ActivityCursorWrapper extends CursorWrapper {
    public ActivityCursorWrapper(Cursor cursor) {
        super(cursor);
    }

    public Activity getActivity() {
        String uuidString = getString(getColumnIndex(ActivityTable.Cols.UUID));
        String activity_type = getString(getColumnIndex(ActivityTable.Cols.ACTIVITY));
        Timestamp timestamp = Timestamp.valueOf(getString(getColumnIndex(ActivityTable.Cols.START_TIME)));

        Activity activity = new Activity(UUID.fromString(uuidString));
        activity.setActivityType(activity_type);
        activity.setTimeStamp(timestamp);

        return activity;
    }
}
