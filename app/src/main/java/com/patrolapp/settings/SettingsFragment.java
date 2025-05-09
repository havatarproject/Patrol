package com.patrolapp.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.preference.EditTextPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.PreferenceFragmentCompat;

import com.patrolapp.utils.BackgroundMusicManager;
import com.patrolapp.utils.EmailHandler;
import com.patrolapp.R;

import java.util.Objects;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = "SettingsFragment";

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);

        SharedPreferences sharedPreferences = getPreferenceManager().getSharedPreferences();
        Objects.requireNonNull(sharedPreferences).registerOnSharedPreferenceChangeListener(this);

        // Initialize preferences
        EditTextPreference quizzesPreference = findPreference("quizzes");
        MultiSelectListPreference selectedQuizzesPreference = findPreference("selected_quizzes");

        // Set up quizzes preference change listener
        if (quizzesPreference != null && selectedQuizzesPreference != null) {
            quizzesPreference.setOnPreferenceChangeListener((preference, newValue) -> {
                String urlList = (String) newValue;
                String[] urls = urlList.split("\\s*,\\s*");

                selectedQuizzesPreference.setEntries(urls);
                selectedQuizzesPreference.setEntryValues(urls);

                return true;
            });

            // Populate selected quizzes if quizzes list is already set
            String urlList = quizzesPreference.getText();
            if (urlList != null && !urlList.isEmpty()) {
                String[] urls = urlList.split("\\s*,\\s*");
                selectedQuizzesPreference.setEntries(urls);
                selectedQuizzesPreference.setEntryValues(urls);
            }
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Preference changed: " + key);

        switch (key) {
            case "background_music":
                handleBackgroundMusicPreference(sharedPreferences);
                break;

            case "log_file":
                handleLogFilePreference(sharedPreferences);
                break;

            case "volume_level_intro":
                handleVolumeLevelIntroPreference(sharedPreferences);
                break;

            default:
                // Handle other preferences if needed
                break;
        }
    }

    private void handleBackgroundMusicPreference(SharedPreferences sharedPreferences) {
        boolean isEnabled = sharedPreferences.getBoolean("background_music", true);
        Log.d(TAG, "Background music enabled: " + isEnabled);

        if (isEnabled) {
            BackgroundMusicManager.getInstance().start(requireContext());
        } else {
            BackgroundMusicManager.getInstance().stop();
        }
    }

    private void handleLogFilePreference(SharedPreferences sharedPreferences) {
        String logFile = sharedPreferences.getString("log_file", "DEFAULT");

        if (!logFile.isEmpty()) {
            EmailHandler.sendEmail(requireContext(), logFile, "Ficheiro de Registo", "Anexo", "time_data.txt");

            // Clear the log file preference after sending the email
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("log_file", "");
            editor.apply();
        }
    }

    private void handleVolumeLevelIntroPreference(SharedPreferences sharedPreferences) {
        String volumeString = sharedPreferences.getString("volume_level_intro", "0.5");
        float volume = Float.parseFloat(volumeString);
        BackgroundMusicManager.getInstance().setQuestionVolume(volume);
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // Additional menu setup if needed
    }
}
