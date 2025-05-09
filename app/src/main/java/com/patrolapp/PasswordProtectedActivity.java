package com.patrolapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.patrolapp.settings.SettingsActivity;

public class PasswordProtectedActivity extends AppCompatActivity {

    private static final String CORRECT_PASSWORD = "0601";
    private EditText passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_protected);

        initViews();
    }

    private void initViews() {
        passwordEditText = findViewById(R.id.passwordEditText);

        Button loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(view -> checkPassword());

        Button backButton = findViewById(R.id.backSelect);
        backButton.setOnClickListener(view -> finish());
    }

    private void checkPassword() {
        String enteredPassword = passwordEditText.getText().toString();
        Bundle extras = getIntent().getExtras();

        if (CORRECT_PASSWORD.equals(enteredPassword)) {
            handleSuccessfulLogin(extras);
        } else {
            Toast.makeText(this, "Incorrect password, please try again", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSuccessfulLogin(Bundle extras) {
        if (extras != null) {
            int value = extras.getInt("key", -1);

            switch (value) {
                case 0:
                    openSettingsActivity();
                    break;
                case 1:
                    openLocationSelectionActivity();
                    break;
                default:
                    Toast.makeText(this, "Contact Admin", Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            Toast.makeText(this, "No action specified, contact Admin", Toast.LENGTH_SHORT).show();
        }
    }

    private void openSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
        finish();
    }

    private void openLocationSelectionActivity() {
        Intent intent = new Intent(this, LocationSelectionActivity.class);
        startActivity(intent);
        finish();
    }
}
