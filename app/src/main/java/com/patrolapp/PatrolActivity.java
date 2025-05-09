package com.patrolapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.patrolapp.utils.LogFileUtil;
import com.robotemi.sdk.Robot;
import com.robotemi.sdk.listeners.OnRobotReadyListener;

import java.util.ArrayList;
import java.util.List;

public class PatrolActivity extends AppCompatActivity implements OnRobotReadyListener {
    // does nothing
    private TextView selectedOptionsText;
    private ArrayList<String> selectedOptions;
    private WebView webView;
    private final int WEB_PAGE_REQUEST_CODE = 1;
    private final boolean status = false;
    Robot robot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        robot = Robot.getInstance();
        robot.addOnRobotReadyListener(this);

        setContentView(R.layout.patrol_activity);

        selectedOptionsText = findViewById(R.id.selected_options_text);
        List<String> selectedOptions = getIntent().getStringArrayListExtra("selectedOptions");

        StringBuilder stringBuilder = new StringBuilder();
        for (String option : selectedOptions) {
            stringBuilder.append(option).append("\n");
        }
        selectedOptionsText.setText(stringBuilder.toString());

        ActivityResultLauncher<Intent> someActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                this::handleActivityResult);

        Button openWebpageButton = findViewById(R.id.open_webpage_button);
        openWebpageButton.setOnClickListener(v -> launchWebpageActivity(someActivityResultLauncher));

        Button backPatrol = findViewById(R.id.backSelect);
        backPatrol.setOnClickListener(v -> startActivity(new Intent(PatrolActivity.this, MainActivity.class)));
    }

    private void handleActivityResult(ActivityResult result) {
        if (result.getResultCode() == Activity.RESULT_OK) {
            Intent data = result.getData();
            assert data != null;
            String webviewResult = data.getStringExtra("result");
            String time = data.getStringExtra("time");

            Log.d("WebResult", webviewResult + "_" + time);

            if ("back_click".equals(webviewResult)) {
                LogFileUtil.appendLogFile(getApplicationContext(), "Back Click ->" + time);
            } else if ("inactivity".equals(webviewResult)) {
                LogFileUtil.appendLogFile(getApplicationContext(), "Inactivity ->" + time);
            }
        }
    }

    private void launchWebpageActivity(ActivityResultLauncher<Intent> launcher) {
        Intent intent = new Intent(PatrolActivity.this, WebViewActivity.class);
        launcher.launch(intent);
        Log.d("RESULT", "end activity webpage");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == WEB_PAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Handle the result from the activity
                Log.d("MyApp", "Activity result OK");
            } else {
                // Handle the case when the activity did not complete successfully
                Log.d("MyApp", "Activity result not OK");
            }
        }
    }

    @Override
    public void onRobotReady(boolean b) {
        List<String> selectedOptions = getIntent().getStringArrayListExtra("selectedOptions");
        Log.d("Selected", selectedOptions.toString());
        List<String> selected = new ArrayList<>();

        Log.d("Patrol?1", selectedOptions.toString()); // [meeting room, francisco desk, poster]
        Log.d("Patrol?2", selected.toString()); // [meeting room, francisco desk, poster]

        sleep();
    }

    private void sleep(){
        try {
            Thread.sleep(9000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
