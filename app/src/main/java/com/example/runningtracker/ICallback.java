package com.example.runningtracker;

import android.location.Location;

// declare callback event function without definition
public interface ICallback {
    void runningTrackerEvent(Location location);
}
