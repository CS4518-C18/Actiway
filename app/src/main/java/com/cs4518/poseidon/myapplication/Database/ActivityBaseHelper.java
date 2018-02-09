package com.cs4518.poseidon.myapplication.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.cs4518.poseidon.myapplication.Database.ActivityDbSchema.ActivityTable;

/**
 * Created by Poseidon on 2/6/18.
 */

public class ActivityBaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "ActivityBaseHelper";
    private static final int VERSION = 2;
    private static final String DATABASE_NAME = "activityBase.db";

    public ActivityBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("create table " + ActivityTable.NAME + "(" +
                " _id integer primary key autoincrement, " +
                ActivityTable.Cols.UUID + ", " +
                ActivityTable.Cols.ACTIVITY + ", " +
                ActivityTable.Cols.START_TIME + " DATETIME DEFAULT CURRENT_TIMESTAMP" +
                ")"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
