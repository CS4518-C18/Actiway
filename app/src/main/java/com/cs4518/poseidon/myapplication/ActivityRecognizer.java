package com.cs4518.poseidon.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.cs4518.poseidon.myapplication.Database.ActivityBaseHelper;
import com.cs4518.poseidon.myapplication.Database.ActivityCursorWrapper;
import com.cs4518.poseidon.myapplication.Database.ActivityDbSchema.ActivityTable;
import com.cs4518.poseidon.myapplication.model.Activity;


/**
 * Created by Poseidon on 2/6/18.
 */

public class ActivityRecognizer {

    private static ActivityRecognizer sActivityRecognizer;

    private Context mContext;
    private SQLiteDatabase mDatabase;

    public static ActivityRecognizer get(Context context) {
        if (sActivityRecognizer == null) {
            sActivityRecognizer = new ActivityRecognizer(context);
        }
        return sActivityRecognizer;
    }

    private ActivityRecognizer(Context context) {
        mContext = context.getApplicationContext();
        mDatabase = new ActivityBaseHelper(mContext)
                .getWritableDatabase();
    }


    public void addActivity(Activity a) {
        ContentValues values = getContentValues(a);
        mDatabase.insert(ActivityTable.NAME, null, values);
    }

    public Activity getLastActivity(){
        Cursor _cursor = mDatabase.rawQuery("SELECT * FROM activities ORDER BY _id DESC LIMIT 1", new String[] {});
        if(_cursor != null && _cursor.moveToFirst()) {
            ActivityCursorWrapper cursor = new ActivityCursorWrapper((_cursor));
            Activity a = cursor.getActivity();
            cursor.close();
            return a;
        };

        //No last activity
        return null;
    }

    private static ContentValues getContentValues(Activity activity) {
        ContentValues values = new ContentValues();
        values.put(ActivityTable.Cols.UUID, activity.getId().toString());
        values.put(ActivityTable.Cols.ACTIVITY, activity.getActivityType());
        values.put(ActivityTable.Cols.START_TIME, activity.getTimeStamp().toString());
        return values;
    }
}
