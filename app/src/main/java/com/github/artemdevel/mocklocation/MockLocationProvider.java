package com.github.artemdevel.mocklocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

class MockLocationProvider {

    private Context context;
    private String providerName;

    MockLocationProvider(Context context, String providerName) {
        this.providerName = providerName;
        this.context = context;
    }

    void setLocation(double lat, double lon, double alt, float accuracy) {
        Location mockLocation = new Location(providerName);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(alt);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setAccuracy(accuracy);
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lm.setTestProviderLocation(providerName, mockLocation);
    }

    void startProvider() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lm.addTestProvider(providerName, false, false, false, false, true, true, true, 0, 5);
        lm.setTestProviderEnabled(providerName, true);
    }

    void stopProvider() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        lm.removeTestProvider(providerName);
    }

}
