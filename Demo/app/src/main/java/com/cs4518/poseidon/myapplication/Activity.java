package com.cs4518.poseidon.myapplication;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by Poseidon on 2/6/18.
 */

public class Activity {
    private UUID mId;
    private String mActivity;
    private Timestamp mTimeStamp;

    public Activity() {
        this(UUID.randomUUID());
    }

    public Activity(UUID id) {
        mId = id;
    }
    public UUID getId() {
        return mId;
    }

    public String getActivityType() {
        return mActivity;
    }

    public void setActivityType(String activity) {
        mActivity = activity;
    }

    public Timestamp getTimeStamp() {
        return mTimeStamp;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        mTimeStamp = timeStamp;
    }

}
