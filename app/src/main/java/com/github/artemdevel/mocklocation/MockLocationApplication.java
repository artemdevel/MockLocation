package com.github.artemdevel.mocklocation;

import android.app.Application;
import android.content.Context;
import android.location.Criteria;
import android.location.LocationManager;

public class MockLocationApplication extends Application {

    private LocationManager locationManager;

    public LocationManager getLocationManager() {
        if (locationManager == null) {
            locationManager = (LocationManager) getApplicationContext()
                    .getSystemService(Context.LOCATION_SERVICE);
            if (locationManager != null) {
                locationManager.addTestProvider(
                        LocationManager.GPS_PROVIDER,
                        false,
                        false,
                        false,
                        false,
                        true,
                        true,
                        true,
                        Criteria.NO_REQUIREMENT,
                        Criteria.ACCURACY_FINE);
                locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
            }
        }
        return locationManager;
    }

}
