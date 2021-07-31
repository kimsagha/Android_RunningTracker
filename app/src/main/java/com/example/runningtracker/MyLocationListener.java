package com.example.runningtracker;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

public class MyLocationListener implements LocationListener {

    public Location myLocation;

    // Location listener to get location longitude and latitude
    @Override
    public void onLocationChanged(Location location) {
        Log.d("g53mdp", location.getLatitude() + " " + location.getLongitude());
        this.myLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // information about the signal, i.e. number of satellites
        Log.d("g53mdp", "onStatusChanged: " + provider + " " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
    // the user enabled (for example) the GPS Log.d("g53mdp", "onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
    // the user disabled (for example) the GPS Log.d("g53mdp", "onProviderDisabled: " + provider);
    }

    public Location getLocation() {
        return myLocation;
    }

}
