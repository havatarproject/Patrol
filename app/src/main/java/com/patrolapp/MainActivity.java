package com.patrolapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.patrolapp.utils.BackgroundMusicManager;
import com.patrolapp.utils.Utils;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.TtsRequest;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnRobotReadyListener {

    private static final int REQUEST_CODE_PERMISSIONS = 10;
    private final String[] permissions = {
            Manifest.permission.CAMERA,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.BLUETOOTH
    };

    private RelativeLayout optionsLayout;
    private ListView optionsList;
    private Button closeButton;
    private ArrayAdapter<String> adapter;
    private Robot robot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!checkAllPermissionsGranted(this)) {
            requestAllPermissions(this);
        }

        robot = Robot.getInstance(); // Get the instance of the robot

        initViews();
        setupListeners();
        BackgroundMusicManager.getInstance().start(getApplicationContext());
        robot.addOnRobotReadyListener(this);
    }

    private void initViews() {
        optionsLayout = findViewById(R.id.options_layout);
        optionsList = findViewById(R.id.options_list);
        closeButton = findViewById(R.id.close_button);
    }

    private void setupListeners() {
        findViewById(R.id.show_options_button).setOnClickListener(v -> openPasswordProtectedActivity(1));
        findViewById(R.id.start_button).setOnClickListener(v -> openHomeMadePatrolActivity());
        findViewById(R.id.settings).setOnClickListener(v -> openPasswordProtectedActivity(0));
        findViewById(R.id.close_app).setOnClickListener(v -> this.finishAffinity());
        findViewById(R.id.back_main).setOnClickListener(v -> toggleOptionsVisibility(false));

        closeButton.setOnClickListener(v -> handleOptionsSelection());
    }

    private void openPasswordProtectedActivity(int key) {
        Intent intent = new Intent(MainActivity.this, PasswordProtectedActivity.class);
        intent.putExtra("key", key);
        startActivity(intent);
    }

    private void openHomeMadePatrolActivity() {
        startActivity(new Intent(MainActivity.this, HomeMadePatrolActivity.class));
    }

    private void handleOptionsSelection() {
        SparseBooleanArray checked = optionsList.getCheckedItemPositions();
        ArrayList<String> selectedOptions = new ArrayList<>();

        for (int i = 0; i < checked.size(); i++) {
            int position = checked.keyAt(i);
            if (checked.valueAt(i)) {
                selectedOptions.add(adapter.getItem(position));
            }
        }

        if (selectedOptions.size() >= 3) {
            Intent intent = new Intent(MainActivity.this, HomeMadePatrolActivity.class);
            intent.putStringArrayListExtra("selectedOptions", selectedOptions);
            intent.putExtra("numberOfTimes", 5);
            startActivity(intent);
        } else {
            Toast.makeText(MainActivity.this, "Please select at least three options.", Toast.LENGTH_SHORT).show();
        }
    }

    private void toggleOptionsVisibility(boolean isVisible) {
        optionsLayout.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
        closeButton.setVisibility(isVisible ? View.VISIBLE : View.INVISIBLE);
    }

    public void requestAllPermissions(Activity activity) {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(),
                    PackageManager.GET_PERMISSIONS
            );
            if (packageInfo.requestedPermissions != null) {
                ActivityCompat.requestPermissions(activity, packageInfo.requestedPermissions, REQUEST_CODE_PERMISSIONS);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public boolean checkAllPermissionsGranted(Activity activity) {
        try {
            PackageInfo packageInfo = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(),
                    PackageManager.GET_PERMISSIONS
            );
            if (packageInfo.requestedPermissions != null) {
                for (String permission : packageInfo.requestedPermissions) {
                    if (ActivityCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                        return false;
                    }
                }
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public void onRobotReady(boolean isReady) {
        if (isReady) {
            adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_multiple_choice, Utils.getOptions(robot));
            optionsList.setAdapter(adapter);
            optionsList.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        } else {
            Toast.makeText(this, "Robot is not ready", Toast.LENGTH_SHORT).show();
        }
    }

    public void onTtsStatusChanged(TtsRequest ttsRequest) {
        Log.d("MainActivity", "TTS status: " + ttsRequest.getStatus());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        BackgroundMusicManager.getInstance().stop();
        robot.removeOnRobotReadyListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (!allPermissionsGranted()) {
                Toast.makeText(this, "Permissions not granted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean allPermissionsGranted() {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    private class MyCountDown extends CountDownTimer {

        public MyCountDown(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
            start();
        }

        @Override
        public void onFinish() {
            robot.goTo("Home Base");
            finish();
        }

        @Override
        public void onTick(long duration) {
            // Implement the logic for each tick if needed
        }
    }
}
