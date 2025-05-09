package com.patrolapp.utils;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.patrolapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private static List<Location> locations;
    private final Context context;
    private OnOptionClickListener optionClickListener;

    public interface OnOptionClickListener {
        void onOptionClick(int position, int option);
    }

    public LocationAdapter(List<Location> locations, Context context) {
        LocationAdapter.locations = getSelectedLocationsFromSharedPreferences(context);
        if (LocationAdapter.locations.isEmpty()) LocationAdapter.locations = locations;
        else {
            for (Location location : locations) {
                if (!LocationAdapter.locations.contains(location)) {
                    LocationAdapter.locations.add(location);
                }
            }
        }
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Location location = locations.get(position);
        holder.textBox.setText(location.getName());
        // Set the text for the button based on the location's state
        switch (location.getSelectionState()) {
            case 0:
                holder.button.setText("Desativado");
                break;
            case 1:
                holder.button.setText("Primária");
                break;
            case 2:
                holder.button.setText("Secundária");
                break;
        }

        // Show the order in the TextView
        if (location.getSelectionState() == 1 || location.getSelectionState() == 2) {
            holder.orderText.setText("Order: " + (location.getOrder() + 1)); // Add 1 to start from 1 instead of 0
            holder.orderText.setVisibility(View.VISIBLE);
        } else {
            // holder.orderText.setVisibility(View.GONE);
            holder.orderText.setText(R.string.not_selected);
        }

        holder.button.setOnClickListener(v -> {
            int currentSelection = location.getSelectionState();
            int newSelection = (currentSelection + 1) % 3;
            location.setSelectionState(newSelection);
                            location.setOrder(9999); //trancada ()
            updateOrders();
            // Show the order in the TextView when the state is selected
            if (location.getSelectionState() == 1 || location.getSelectionState() == 2) {
                holder.orderText.setText("Order: " + (location.getOrder() + 1));
                holder.orderText.setVisibility(View.VISIBLE);
            } else {
                holder.orderText.setText(R.string.not_selected);
                //holder.orderText.setVisibility(View.GONE);
            }

            notifyDataSetChanged(); // Update the UI to reflect the new selection state
            saveSelectedLocationsToSharedPreferences();
        });
    }

    private void updateOrders() {
        List<Location> updatedLocations = new ArrayList<>();
        for (Location location : locations) {
            if (location.isSelected()) {
                updatedLocations.add(location);
            }
        }

        int n = updatedLocations.size();
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < n - i - 1; j++) {
                if (updatedLocations.get(j).getOrder() > updatedLocations.get(j + 1).getOrder()) {
                    Location temp = updatedLocations.get(j);
                    updatedLocations.set(j, updatedLocations.get(j + 1));
                    updatedLocations.set(j + 1, temp);
                }
            }
        }

        for (int i = 0; i < updatedLocations.size(); i++) {
            updatedLocations.get(i).setOrder(i);
        }

        for (Location updatedLocation : updatedLocations) {
            for (Location location : locations) {
                if (updatedLocation.getName().equals(location.getName())) {
                    location.setOrder(updatedLocation.getOrder());
                }
            }
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return locations.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView orderText;
        Button button;

        TextView textBox;
        int currentState; // To track the current state

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            orderText = itemView.findViewById(R.id.orderText);
            button = itemView.findViewById(R.id.button);
            orderText = itemView.findViewById(R.id.orderText);
            textBox = itemView.findViewById(R.id.textBox);

            currentState = 0; // 0: Unchecked/Desativado, 1: Primária, 2: Secundaria
            updateButtonState();
            button.setOnClickListener(v -> {
                currentState = (currentState + 1) % 3;
                updateButtonState();
            });
        }

        @SuppressLint("SetTextI18n")
        private void updateButtonState() {
            // Update the button text based on the current state
            switch (currentState) {
                case 0:
                    button.setText("Desativado");
                    break;
                case 1:
                    button.setText("Primária");
                    break;
                case 2:
                    button.setText("Secundária");
                    break;
            }
        }
    }

    private void saveSelectedLocationsToSharedPreferences() {
        SharedPreferences preferences = context.getSharedPreferences("SelectedLocations", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();

        for (Location location : locations) {
            editor.putInt(location.getName() + "_order", location.getOrder());
            editor.putInt(location.getName() + "_state", location.getSelectionState());
        }

        editor.apply();
    }

    public static List<Location> getSelectedLocationsFromSharedPreferences(Context context) {
        List<Location> selectedLocations = new ArrayList<>();
        SharedPreferences preferences = context.getSharedPreferences("SelectedLocations", MODE_PRIVATE);

        Map<String, ?> allEntries = preferences.getAll();
        for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
            String key = entry.getKey();
            if (key.endsWith("_order")) {
                String locationName = key.substring(0, key.length() - 6);
                int order = preferences.getInt(locationName + "_order", 0);
                int state = preferences.getInt(locationName + "_state", 0);
                Location location = new Location(locationName, order, state);
                selectedLocations.add(location);
            }
        }

        return selectedLocations;
    }

    public List<Location> getSelectedLocations() {
        List<Location> selectedLocations = new ArrayList<>();
        for (Location location : locations) {
            if (location.isSelected()) {
                selectedLocations.add(location);
            }
        }
        return selectedLocations;
    }

    static public Location getLocationByOrder(List<Location> locations, int order) {
        for (Location location : locations) {
            if (location.getOrder() == order) {
                return location;
            }
        }
        return null;
    }
}
