package com.patrolapp;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import com.patrolapp.utils.Location;
import com.patrolapp.utils.LocationAdapter;
import com.patrolapp.utils.LogFileUtil;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnBeWithMeStatusChangedListener;
import com.robotemi.sdk.listeners.OnGoToLocationStatusChangedListener;
import com.robotemi.sdk.listeners.OnRobotReadyListener;
import com.robotemi.sdk.navigation.model.SpeedLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeMadePatrolActivity extends AppCompatActivity implements
        OnRobotReadyListener, OnGoToLocationStatusChangedListener, OnBeWithMeStatusChangedListener {
    private Robot robot;
    private boolean finished;
    private int step; // step on secondary locations
    private HandlerThread handlerThread;
    Runnable firstTiltAction;
    Runnable secondTiltAction;
    Runnable delayedAction;
    private boolean stop;
    private int actualPosition = 0;
    private int numberOfRounds;
    private int turn_seconds_first;
    private int turn_seconds_second;

    private int turn_angle_first;
    private int turn_angle_second;
    private int stop_seconds_primary;
    private int tilt_main_stop;
    private int tilt_secondary_stop;
    private int follow_up_seconds;

    private final int WEB_PAGE_REQUEST_CODE = 1;
    List<Location> selectedLocations;
    Handler handler = new Handler();

    ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                Log.d("WebActivityResult", String.valueOf(result.getResultCode()));
                if (result.getResultCode() == Activity.RESULT_CANCELED) {
                }
            });

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.patrol_activity);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        robot = Robot.getInstance();

        Button answerButton = findViewById(R.id.open_webpage_button);
        Button backButton = findViewById(R.id.backSelect);

        List<Location> selecLocations = LocationAdapter.getSelectedLocationsFromSharedPreferences(this);
        selectedLocations = new ArrayList<>();
        for (Location location : selecLocations) {
            if (location.isSelected()) selectedLocations.add(location);
        }
        //selectedLocations = LocationAdapter.getSelectedLocationsFromSharedPreferences(this);
        numberOfRounds = Integer.parseInt(sharedPreferences.getString("number_of_rounds", "5"));
        stop_seconds_primary = Integer.parseInt(sharedPreferences.getString("stop_seconds_primary", "5"));
        tilt_main_stop = Integer.parseInt(sharedPreferences.getString("tilt_main_stop", "5"));
        tilt_secondary_stop = Integer.parseInt(sharedPreferences.getString("tilt_secondary_stop", "30"));
        turn_seconds_first = Integer.parseInt(sharedPreferences.getString("turn_seconds_first", "5"));
        turn_seconds_second = Integer.parseInt(sharedPreferences.getString("turn_seconds_second", "5"));
        turn_angle_first = Integer.parseInt(sharedPreferences.getString("turn_angle_first", "45"));
        turn_angle_second = Integer.parseInt(sharedPreferences.getString("turn_angle_second", "-45"));
        follow_up_seconds = Integer.parseInt(sharedPreferences.getString("follow_up_seconds", "5"));

        setText(selectedLocations);

        answerButton.setOnClickListener(v -> {
            Intent intent = new Intent(HomeMadePatrolActivity.this, WebViewActivity.class);
            someActivityResultLauncher.launch(intent);
            //robot.beWithMe();

            Log.d("Webpage", "Activity init");
        });

        backButton.setOnClickListener(view -> {
            if (delayedAction != null) handler.removeCallbacks(delayedAction);
            if (firstTiltAction != null) handler.removeCallbacks(firstTiltAction);
            if (secondTiltAction != null) handler.removeCallbacks(secondTiltAction);
            finish();
        });
    }

    private void setText(List<Location> selectedOptions){
        TextView selectedOptionsText = findViewById(R.id.selected_options_text);
        StringBuilder stringBuilder = new StringBuilder();
        for (Location location : selectedOptions) {
            stringBuilder.append(location.getOrder()).append(")").append(location.getName()).append(":").append(location.getSelectionState()).append("\n");
        }
        selectedOptionsText.setText(stringBuilder.toString());
    }

    @Override
    protected synchronized void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("MyApp", String.valueOf(requestCode));
        if (resultCode == RESULT_OK) {
            // Handle the result from the activity
            String webview_result = data.getStringExtra("result");
            Log.d("MyApp", "Activity result OK");
            if (webview_result.equals("back_click")) {
                LogFileUtil.appendLogFile(getApplicationContext(), "Back Click ->" + data.getStringExtra("time"));
            } else if (webview_result.equals("inactivity")) {
                LogFileUtil.appendLogFile(getApplicationContext(), "Inactivity ->" + data.getStringExtra("time"));
            }
        } else {
            // Handle the case when the activity did not complete successfully
            Log.d("MyApp", "Activity result not OK");
        }
    }

    private void nextMovement(Location nextLocation, int numberOfRounds, int actualPosition) {
        synchronized (robot) {
            if (nextLocation.getSelectionState() == 1) { // primary
                goAndWait2(Objects.requireNonNull(nextLocation));
            } else if (nextLocation.getSelectionState() == 2) { // secondary
                goAndWait2(Objects.requireNonNull(nextLocation));
            } else {
                robot.goTo(nextLocation.getName());
            }
        }
    }

    private void move2(List<Location> selectedLocations, int times, int resumePosition){
        int position = resumePosition % selectedLocations.size();
        synchronized (robot) {
            Location location = LocationAdapter.getLocationByOrder(selectedLocations, position);
            assert location != null;
            if (location.getSelectionState() == 1) { // primary
                goAndWait(Objects.requireNonNull(location).getName());
            } else if (location.getSelectionState() == 2) { // secondary
                goAndWait(Objects.requireNonNull(location).getName());
            }
        }
    }

    private void turnMovement(int firstAngle, int secondAngle, float speed) {
        firstTiltAction = () -> turn(firstAngle, speed);
        handler.postDelayed(firstTiltAction, 0L);
        Log.d("delayedTurn1", String.valueOf(turn_seconds_first * 1000L));
        secondTiltAction = () -> turn(secondAngle, speed);
        handler.postDelayed(secondTiltAction, turn_seconds_first * 1000L);
        Log.d("delayedTurn2", String.valueOf(turn_seconds_first * 1000L));
    }

    private void turn(int angle, float speed) {
        robot.turnBy(angle, speed);
        robot.tiltAngle(tilt_secondary_stop);
        Log.d("Turn", String.valueOf(angle));
    }

    private void move(List<String> selectedOptions, int times, int resumePosition){
        int position = resumePosition % selectedOptions.size();
        synchronized (robot) {
            goAndWait(selectedLocations.get(position).getName());
        }
    }

    private void moveToLocation(List<Location> selectedOptions, int times, int resumePosition){
        int position = resumePosition % selectedOptions.size();
        synchronized (robot) {
            goAndWait(getLocationByOrder(selectedOptions, position).getName());
        }
    }

    private Location getLocationByOrder(List<Location> selectedOptions, int order) {
        for (Location location : selectedOptions) {
            if (location.getOrder() == order) {
                return location;
            }
        }
        return null; // Return null if not found
    }

    public void goAndWait(String location){
        robot.goTo(location, false, false, SpeedLevel.SLOW);
        robot.tiltBy(10);
        while(!finished){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            robot.tiltBy(10);
            Log.d("notFinished", finished + "_" );
        }


    }

    public void goAndWait2(Location location){
        robot.goTo(location.getName(), false, false, SpeedLevel.SLOW);
        robot.tiltBy(10);
        while(!finished){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            robot.tiltBy(10);
            Log.d("notFinished", finished + "_" );
        }
    }

    private void sleep(int ms){
        try{
            Thread.sleep(ms);
            Log.d("sleep", ms + "_" );
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public synchronized void onGoToLocationStatusChanged(@NonNull String location, @NonNull String status, int descriptionId, @NonNull String description) {
        Log.d("StatusChanged", status);
        robot.tiltAngle(50);

        switch (status) {
            case OnGoToLocationStatusChangedListener.START:
                finished = false;
                break;

            case OnGoToLocationStatusChangedListener.CALCULATING:
                //robot.speak(TtsRequest.create("Calculating", false));
                break;

            case OnGoToLocationStatusChangedListener.GOING:
                //robot.speak(TtsRequest.create("Going", false));
                break;

            case OnGoToLocationStatusChangedListener.COMPLETE:
                //robot.speak(TtsRequest.create("Completed", false));
                // current position, in case of secondary locations

                int currentPosition = actualPosition % selectedLocations.size();
                Location currentLocation = LocationAdapter.getLocationByOrder(selectedLocations, currentPosition);
                // if the current location is secondary, just move after tilt
                actualPosition++;
                finished = true;
                robot.tiltAngle(tilt_main_stop);
                int position = actualPosition % selectedLocations.size();
                float round = (float) actualPosition / selectedLocations.size();
                Location nextLocation;
                if (round == numberOfRounds) {
                    nextLocation = new Location("home base");
                } else if (round > numberOfRounds) {
                    break;
                } else {
                    nextLocation = LocationAdapter.getLocationByOrder(selectedLocations, position);
                }

                long delay;
                assert currentLocation != null;
                if (currentLocation.getSelectionState() == 2) {
                    // tilt an them move
                    Log.d("TiltMovement:", currentLocation.getName());
                    turnMovement(turn_angle_first, turn_angle_second, (float) 1);
                    //tilt();
                    delay = turn_seconds_first + turn_seconds_second;
                } else { // state = 1
                    delay = stop_seconds_primary;
                }

                assert nextLocation != null;
                delayedAction = () -> nextMovement(nextLocation, numberOfRounds, actualPosition);
                handler.postDelayed(delayedAction, delay * 1000L);

                //update listener
                Button answerButton = findViewById(R.id.open_webpage_button);
                answerButton.setOnClickListener(v -> {
                    handler.removeCallbacks(delayedAction);
                    if (secondTiltAction != null) handler.removeCallbacks(secondTiltAction);
                    if (firstTiltAction != null) handler.removeCallbacks(firstTiltAction);

                    Intent intent = new Intent(HomeMadePatrolActivity.this, WebViewActivity.class);
                    someActivityResultLauncher.launch(intent);
                    Log.d("Webpage", "Activity init");
                });

                break;
            case OnGoToLocationStatusChangedListener.ABORT:
                //robot.speak(TtsRequest.create("Gostava de responder a um questionário?", false, TtsRequest.Language.valueToEnum(14)));
                finished = true;
                robot.tiltAngle(55);
                //robot.tiltAngle(-15);
                //finished = true;
                break;
        }
    }


    @Override
    public void onRobotReady(boolean b) {
        Log.d("On robot ready", String.valueOf(b));
    }

    @Override
    protected synchronized void onStart() {
        Log.d("onStart", "a");
        super.onStart();
        robot.addOnGoToLocationStatusChangedListener(this);
        robot.addOnRobotReadyListener(this);
        Robot.getInstance().addOnRobotReadyListener(this);
        Robot.getInstance().addOnGoToLocationStatusChangedListener(this);
    }

    @Override
    public synchronized void onResume(){
        Log.d("onResume ", String.valueOf(this));
        super.onResume();
        handlerThread = new HandlerThread("inference");
        handlerThread.start();
        handler = new Handler(handlerThread.getLooper());
        finished = true;
        robot.tiltAngle(50);
        int position = actualPosition % selectedLocations.size();
        Location nextLocation = LocationAdapter.getLocationByOrder(selectedLocations, position);
        assert nextLocation != null;
        nextMovement(nextLocation, numberOfRounds, actualPosition);
    }

    @Override
    public synchronized void onPause() {
        Log.d("onPause ", String.valueOf(this));
        handlerThread.quitSafely();
        try {
            handlerThread.join();
            handlerThread = null;
            handler = null;
        } catch (final InterruptedException e) {
            Log.e(String.valueOf(e), "Exception!");
        }

        super.onPause();
        robot.stopMovement();
    }

    @Override
    protected synchronized void onStop() {
        Log.d("onStop", "");
        super.onStop();
        robot.removeOnRobotReadyListener(this);
        robot.removeOnGoToLocationStatusChangedListener(this);
        Robot.getInstance().removeOnRobotReadyListener(this);
        Robot.getInstance().removeOnGoToLocationStatusChangedListener(this);
    }

    private void printToast(Context context, String text) {
        Toast toast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
        toast.show();
    }

    @Override
    public void onBeWithMeStatusChanged(@NonNull String status) {
        switch(status) {
            case OnBeWithMeStatusChangedListener.ABORT:
                break;
            case OnBeWithMeStatusChangedListener.CALCULATING:
                break;
            case OnBeWithMeStatusChangedListener.SEARCH:
                break;
            case OnBeWithMeStatusChangedListener.TRACK:
                break;
            case OnBeWithMeStatusChangedListener.OBSTACLE_DETECTED:
                break;

        }
    }
}
