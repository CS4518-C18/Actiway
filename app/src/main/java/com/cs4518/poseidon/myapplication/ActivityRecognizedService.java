package com.cs4518.poseidon.myapplication;

import android.app.IntentService;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.cs4518.poseidon.myapplication.model.Activity;
import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.sql.Timestamp;

/**
 * @author Poseidon
 * @version Feb 6, 2018
 */

public class ActivityRecognizedService extends IntentService {

    public ActivityRecognizedService() {
        super("ActivityRecognizedService");
    }

    public ActivityRecognizedService(String name) {
        super(name);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);
            //handleDetectedActivities(result.getProbableActivities());
            handleDetectedActivities(result);
        }
    }


    final static String RUNNING = "Running";
    final static String STILL = "Still";
    final static String WALKING = "Walking";
    final static String UNKNOWN = "Unknown";

    // private void handleDetectedActivities(List<DetectedActivity> probableActivities) {
    private void handleDetectedActivities(ActivityRecognitionResult result){
        //On recognition of activity
        final Activity lastActivity = ActivityRecognizer.get(getApplicationContext()).getLastActivity();
        Activity currentActivity = new Activity();
        currentActivity.setActivityType(UNKNOWN);

        // Determine the most probable activity (our four required activities should not overlap significantly enough)
        for( DetectedActivity activity : result.getProbableActivities() ) {
            switch( activity.getType() )
            {
                case DetectedActivity.RUNNING: {
                    Log.e( "ActivityRecogition", "Running: " + activity.getConfidence() );
                    if(activity.getConfidence() >= 75) {
                        currentActivity.setActivityType(RUNNING);
                    }
                    break;
                }
                case DetectedActivity.STILL: {
                    Log.e( "ActivityRecogition", "Still: " + activity.getConfidence() );
                    if(activity.getConfidence() >= 75) {
                        currentActivity.setActivityType(STILL);
                    }
                    break;
                }

                case DetectedActivity.WALKING: {
                    Log.e( "ActivityRecogition", "Walking: " + activity.getConfidence() );
                    if( activity.getConfidence() >= 75 ) {
                        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
                        builder.setContentText( "Are you walking?" );
                        builder.setSmallIcon( R.mipmap.ic_launcher );
                        builder.setContentTitle( getString( R.string.app_name ) );
                        NotificationManagerCompat.from(this).notify(0, builder.build());
                    }

                    if(activity.getConfidence() >= 75) {
                        currentActivity.setActivityType(WALKING);
                    }
                    break;
                }
                case DetectedActivity.UNKNOWN: {
                    Log.e( "ActivityRecogition", "Unknown: " + activity.getConfidence() );
                    if(activity.getConfidence() >= 75) {
                        currentActivity.setActivityType(UNKNOWN);
                    }
                    break;
                }
            }

        }
          /*if(mostProbable.getConfidence() >= 75) { // To confirm an activity is happening, we need a confidence level of 75% or greater
              switch(mostProbable.getType()) {
                  case DetectedActivity.WALKING:
                      currentActivity.setActivityType(WALKING);
                      break;
                  case DetectedActivity.IN_VEHICLE:
                      currentActivity.setActivityType(IN_VEHICLE);
                      break;

                  case DetectedActivity.STILL:
                      currentActivity.setActivityType(STILL);
                      break;

                  case DetectedActivity.RUNNING:
                      currentActivity.setActivityType(RUNNING);
                      break;
              }
          } else {
            Log.e("handleDetectedActivity", mostProbable.getType() + " detected with confidence " + mostProbable.getConfidence());
          }*/



        //currentActivity.setActivityType(WALKING);

        // Note the timestamp to determine duration of activity
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        currentActivity.setTimeStamp(timestamp);
        boolean isDifferentActivity = false;

        // Initialize our duration values
        int seconds = 0;
        int minutes = 0;
        int hours = 0;

        // Check if this is our first activity- if so, we treat it as a different activity
        if(lastActivity == null) {
            isDifferentActivity = true;
        }
        else {
            // If not, we need to check if the current and past activities are equal- if so, make no changes
            if (!lastActivity.getActivityType().equals(currentActivity.getActivityType())) {
                // If not, note the time for the new activity
                isDifferentActivity = true;
                long nowTime = currentActivity.getTimeStamp().getTime();
                long timestampTime = lastActivity.getTimeStamp().getTime();
                long millDiff = nowTime - timestampTime;

                // Calculate the duration in seconds, minutes, and hours
                seconds = (int) (millDiff / 1000) % 60 ;
                minutes = (int) ((millDiff / (1000*60)) % 60);
                hours   = (int) ((millDiff / (1000*60*60)) % 24);

            }
        }

        // If the activities are different, input the current one into our database
        if(isDifferentActivity){

            //Add to database if it's a different activity
            ActivityRecognizer.get(getApplicationContext()).addActivity(currentActivity);

            //Make these accessible below; internal classes need finalized vars
            final int seconds2 = seconds;
            final int hours2 = hours;
            final int minutes2 = minutes;

            //Send a toast with the previous information
            Handler handler = new Handler(Looper.getMainLooper());
            handler.post(new Runnable() {
                public void run() {
                    String verb = "";
                    String toastString;

                    // swap activity cases to get the proper verb for the toast
                    if(lastActivity != null) {
                        switch (lastActivity.getActivityType())
                        {

                            case WALKING:
                                verb = "walking";
                                break;
                            case STILL:
                                verb = "sitting still";
                                break;
                            case RUNNING:
                                verb = "running";
                                break;
                            case UNKNOWN:
                                verb = "unknowning";
                                break;
                        }

                        toastString = "You were just " + verb + " for " + hours2 + " hours," + minutes2 + " minutes, and " + seconds2 + " seconds.";
                    }
                    else{
                        toastString = "Who knows what your were doing (No last activity recorded.)";
                    }

                    //Finally make the toast
                    Toast.makeText(getApplicationContext(), toastString, Toast.LENGTH_LONG).show();
                }
            });

            //inside ActivityRecognitionIntentService 's onHandleIntent
            Intent broadcastIntent = new Intent();

            // Give it the category for all intents sent by the Intent Service
            broadcastIntent.addCategory("SampleCategory");
            // Set the action and content for the broadcast intent
            broadcastIntent.setAction("PleaseWork");
            broadcastIntent.putExtra("Activity", currentActivity.getActivityType());

            // Broadcast *locally* to other components in this app
            LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);

        } else {
            Log.e("idk", "Not changing activities");
        }
    }
}
