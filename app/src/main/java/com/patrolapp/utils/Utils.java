package com.patrolapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.robotemi.sdk.Robot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Utils {

    public static String[] getOptions(Robot robot) {
        if (robot.isReady()) {
            List<String> locations = robot.getLocations();
            for (String location : locations) {
                Log.d("TAG", "Location name: " + location);
            }
        } else {
            Log.e("TAG", "Robot is not connected");
        }
        List<String> locations = robot.getLocations();
        locations.remove("home base"); // not considered
        int size = locations.size();
        String[] array = locations.toArray(new String[size]);

        return array;
    }
    public static List<Location> getLocations(Robot robot) {
        if (robot.isReady()) {
            List<String> locations = robot.getLocations();
            for (String location : locations) {
                Log.d("TAG", "Location name: " + location);
            }
        } else {
            Log.e("TAG", "Robot is not connected");
        }

        List<String> locations = robot.getLocations();
        List<Location> locationsList = new ArrayList<>();

        // Iterate through the list of strings and create Location objects
        for (String locationName : locations) {
            Location location = new Location(locationName);
            locationsList.add(location);
        }

        return locationsList;
    }

    static List<Location> retrieveSelectedLocations(Context context) {
        SharedPreferences preferences = context.getSharedPreferences("SelectedLocations", Context.MODE_PRIVATE);

        List<Location> selectedLocations = new ArrayList<>();

        for (Map.Entry<String, ?> entry : preferences.getAll().entrySet()) {
            String locationName = entry.getKey();
            int order = (int) entry.getValue();

            Location location = new Location(locationName);
            //location.setSelected(true);
            location.setOrder(order);

            selectedLocations.add(location);
        }
         return selectedLocations;
    }

}
