package com.github.artemdevel.mocklocation;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.SystemClock;

class MockLocationProvider {

    private static final float DEFAULT_ACCURACY = 1.f;

    private final Context context;

    MockLocationProvider(Context context) {
        this.context = context;
    }

    void setLocation(double lat, double lon, double alt) {
        Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(alt);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setAccuracy(DEFAULT_ACCURACY);
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lm.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
        }
    }

    void startProvider() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lm.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, true, true, true, 0, 5);
            lm.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
        }
    }

    void stopProvider() {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (lm != null) {
            lm.removeTestProvider(LocationManager.GPS_PROVIDER);
        }
    }

}
