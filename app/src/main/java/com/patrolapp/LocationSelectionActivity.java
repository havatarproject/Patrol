package com.patrolapp;

import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patrolapp.utils.Location;
import com.patrolapp.utils.LocationAdapter;
import com.patrolapp.utils.Utils;
import com.robotemi.sdk.Robot;

import java.util.List;

public class LocationSelectionActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_selected);

        initializeViews();
        setupRecyclerView();
    }

    private void initializeViews() {
        Button closeButton = findViewById(R.id.backSelect);
        closeButton.setOnClickListener(v -> onBackPressed());
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Robot robot = Robot.getInstance();
        List<Location> locations = Utils.getLocations(robot);

        LocationAdapter adapter = new LocationAdapter(locations, this);
        recyclerView.setAdapter(adapter);
    }
}
