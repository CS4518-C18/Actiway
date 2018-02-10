package com.cs4518.poseidon.myapplication;

/**
 * @author Haofan
 * @version Feb 9, 2018
 */

public class CustomGeofence {
    protected String name;
    protected Boolean inForTheFirstTime = true;
    protected Boolean finishedSixSteps = false;
    protected float initialStepInGeofence = 0;
    protected Boolean outForTheFirstTime = false;

    // numEnteredGeofence
    protected int numEnteredGeofence = 0;
    protected final String SHARED_PREF_KEY;

    public CustomGeofence (String name, int numEntered, String sharedPrefKey) {
        this.name = name;
        this.numEnteredGeofence = numEntered;
        this.SHARED_PREF_KEY = sharedPrefKey;
    }
}
