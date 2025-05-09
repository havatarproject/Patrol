package com.patrolapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceManager;

import com.patrolapp.utils.BackgroundMusicManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;

public class WebViewActivity extends AppCompatActivity {

    private static final String TAG = "WebViewActivity";
    private static final long DISCONNECT_TIMEOUT = 30 * 1000L; // 30 seconds

    private Button backButton;
    private TextView textView;

    private Handler handler;
    private Runnable inactivityRunnable;
    private CountDownTimer inactivityTimer;

    private Handler timeHandler;
    private long startTime;
    private Runnable timerRunnable;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.web_view);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int inactivityTimeout = Integer.parseInt(sharedPreferences.getString("inactivity_timeout", "30"));

        WebView webView = findViewById(R.id.webView1);
        backButton = findViewById(R.id.backButton);
        textView = findViewById(R.id.ticTime);

        setupWebView(webView, sharedPreferences);
        setupBackgroundMusic();
        setupBackButton();

        startTime = System.currentTimeMillis();
        startInactivityHandler(inactivityTimeout);

        handler = new Handler();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopTimer();
    }

    @Override
    public void onUserInteraction() {
        super.onUserInteraction();
        resetInactivityHandler();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setupWebView(WebView webView, SharedPreferences sharedPreferences) {
        clearWebViewData(webView);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUseWideViewPort(true);

        Set<String> selectedQuizzes = sharedPreferences.getStringSet("selected_quizzes", null);
        if (selectedQuizzes != null && !selectedQuizzes.isEmpty()) {
            List<String> quizList = new ArrayList<>(selectedQuizzes);
            String randomQuizUrl = quizList.get(new Random().nextInt(quizList.size()));
            webView.loadUrl(randomQuizUrl);
        }
    }

    private void clearWebViewData(WebView webView) {
        WebStorage.getInstance().deleteAllData();
        CookieManager.getInstance().removeAllCookies(null);
        CookieManager.getInstance().flush();

        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearSslPreferences();
    }

    private void setupBackgroundMusic() {
        BackgroundMusicManager musicManager = BackgroundMusicManager.getInstance();
        musicManager.pause();
        musicManager.startQuestion(getApplicationContext());
    }

    @SuppressLint("UnsafeIntentLaunch")
    private void setupBackButton() {
        backButton.setOnClickListener(view -> {
            stopInactivityHandler();
            Intent returnIntent = getIntent();
            returnIntent.putExtra("result", "back_click");
            returnIntent.putExtra("time", getElapsedTime());
            setResult(Activity.RESULT_OK, returnIntent);
            BackgroundMusicManager.getInstance().resume(getApplicationContext());
            finish();
        });
    }

    private void startInactivityHandler(int delayInSeconds) {
        inactivityRunnable = this::startInactivityTimer;
        handler.postDelayed(inactivityRunnable, delayInSeconds * 1000L);
    }

    private void startInactivityTimer() {
        inactivityTimer = new CountDownTimer(10000L, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textView.setText(String.format(Locale.getDefault(), "Segundos restantes: %d", millisUntilFinished / 1000));
                textView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onFinish() {
                handleInactivityTimeout();
            }
        };
        inactivityTimer.start();
        Toast.makeText(WebViewActivity.this, "Clique no ecrã para evitar a saída do questionário.", Toast.LENGTH_SHORT).show();
    }

    private void handleInactivityTimeout() {
        stopInactivityHandler();
        @SuppressLint("UnsafeIntentLaunch") Intent returnIntent = getIntent();
        returnIntent.putExtra("result", "inactivity");
        returnIntent.putExtra("time", getElapsedTime());
        setResult(Activity.RESULT_OK, returnIntent);
        BackgroundMusicManager.getInstance().resume(getApplicationContext());
        finish();
    }

    private void stopInactivityHandler() {
        handler.removeCallbacks(inactivityRunnable);
        if (inactivityTimer != null) {
            inactivityTimer.cancel();
        }
        textView.setVisibility(View.INVISIBLE);
    }

    private void resetInactivityHandler() {
        stopInactivityHandler();
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        int delay = Integer.parseInt(sharedPreferences.getString("inactivity_timeout", "30"));
        startInactivityHandler(delay);
    }

    private void startTimer() {
        timeHandler = new Handler();
        timerRunnable = new Runnable() {
            @Override
            public void run() {
                updateTimer();
                timeHandler.postDelayed(this, 1000);
            }
        };
        timeHandler.postDelayed(timerRunnable, 1000);
    }

    private void stopTimer() {
        timeHandler.removeCallbacks(timerRunnable);
    }

    private void updateTimer() {
        // Implementation for updating the UI with the elapsed time, if needed
    }

    private String getElapsedTime() {
        long elapsedTime = System.currentTimeMillis() - startTime;
        int seconds = (int) (elapsedTime / 1000);
        return getCurrentTime() + " Elapsed: " + seconds + " seconds\n";
    }

    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy_HH:mm:ss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
