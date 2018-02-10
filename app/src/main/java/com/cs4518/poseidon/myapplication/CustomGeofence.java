package com.cs4518.poseidon.myapplication;

/**
 * Created by haofanzhang on 2/9/18.
 */

public class CustomGeofence {
    protected String name;
    protected Boolean forTheFirstTime = true;
    protected Boolean finishedSixSteps = false;
    protected float initialStepInGeofence = 0;

    // numEnteredGeofence
    protected int numEnteredGeofence = 0;
    protected final String SHARED_PREF_KEY;

    public CustomGeofence (String name, int numEntered, String sharedPrefKey) {
        this.name = name;
        this.numEnteredGeofence = numEntered;
        this.SHARED_PREF_KEY = sharedPrefKey;
    }
}
