package com.patrolapp.utils;

import java.util.Objects;

public class Location {
    private final String name;
    private int order;
    private int selectionState;
    public Location(String name) {
        this.name = name;
        this.order = -1;
        this.selectionState = 0;
    }

    public Location(String name, int order, int selectionState) {
        this.name = name;
        this.order = order;
        this.selectionState = selectionState; // 0 unselected, 1 primary, 2 secondary
    }
    public String getName() {
        return this.name;
    }
    public void setSelectionState(int selectionState) {
        this.selectionState = selectionState;
    }
    public int getSelectionState() {
        return this.selectionState;
    }

    public boolean isSelected() {
        return (this.selectionState == 1 || this.selectionState == 2);
    }

    public int getOrder() {
        return this.order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Location location = (Location) obj;
        return name.equals(location.name); //equals based on name and nor order/state
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, order, selectionState);
    }
}
