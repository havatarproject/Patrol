package com.patrolapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.util.Log;

import com.patrolapp.R;

public class BackgroundMusicManager {
    private static BackgroundMusicManager instance;
    private MediaPlayer mediaPlayer;
    private AudioManager audioManager;

    private MediaPlayer mediaPlayer2;
    private AudioManager audioManager2;


    private BackgroundMusicManager() {
        // Private constructor to prevent instantiation
    }

    public static synchronized BackgroundMusicManager getInstance() {
        if (instance == null) {
            instance = new BackgroundMusicManager();
        }
        return instance;
    }

    public void start(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("com.patrolapp_preferences", Context.MODE_PRIVATE);
        Log.d("MUSIC_MANAGER", String.valueOf(sharedPreferences.getBoolean("background_music", true)));
        if (!sharedPreferences.getBoolean("background_music", true)) {
            return;
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.background);
            mediaPlayer.setLooping(true);
        }

        if (audioManager == null) {
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        // Set your desired volume percentage here
        //float volumePercentage = Float.parseFloat(sharedPreferences.getString("volume_level_background", "0.5"));
        int desiredVolume = defaultVolume;
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0);
        mediaPlayer.start();
    }

    public void startQuestion(final Context context) {
        final SharedPreferences sharedPreferences = context.getSharedPreferences("com.patrolapp_preferences", Context.MODE_PRIVATE);

        final AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        final int previousVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (mediaPlayer2 == null) {
            mediaPlayer2 = MediaPlayer.create(context.getApplicationContext(), R.raw.inicio);
        }

        if (audioManager2 == null) {
            audioManager2 = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        int maxVolume = audioManager2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        float volumePercentage = Float.valueOf(sharedPreferences.getString("volume_level_intro", "0.5"));
        int desiredVolume = (int) (maxVolume * volumePercentage);
        audioManager2.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0);

        mediaPlayer2.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // Reset volume to previous level
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, previousVolume, 0);
                // Release media player resources
                mediaPlayer2.release();
                mediaPlayer2 = null;
            }
        });

        mediaPlayer2.start();
    }

    public void pause() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void resume(Context context) {
        try {
            SharedPreferences sharedPreferences = context.getSharedPreferences("com.example.patrolapp_preferences", Context.MODE_PRIVATE);
            if (!sharedPreferences.getBoolean("background_music", true)) {
                return;
            }
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int defaultVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

            //float volumePercentage = sharedPreferences.getFloat("volume_level_background", 0.5f);
            //int desiredVolume = (int) (maxVolume * volumePercentage);
            int desiredVolume = defaultVolume;
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0);

            if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
                mediaPlayer.start();
            }
        } catch (Exception e){
            // ignore
        }
    }

    public void stop() {
        Log.d("MUSIC_MANAGER_STOP", "");

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (mediaPlayer2 != null) {
            mediaPlayer2.stop();
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }
    }

    public void setVolume(float volumePercentage) {
        if (audioManager != null) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int desiredVolume = (int) (maxVolume * volumePercentage);
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0);
        }
    }

    public void setQuestionVolume(float volumePercentage) {
        if (audioManager2 != null) {
            int maxVolume = audioManager2.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int desiredVolume = (int) (maxVolume * volumePercentage);
            audioManager2.setStreamVolume(AudioManager.STREAM_MUSIC, desiredVolume, 0);
        }
    }

}
