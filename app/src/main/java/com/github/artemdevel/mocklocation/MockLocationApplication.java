package com.github.artemdevel.mocklocation;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;

public final class MockLocationApplication extends Application {

    private LocationManager locationManager;

    public LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
        return locationManager;
    }

}
