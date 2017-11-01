package com.github.artemdevel.mocklocation;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

public class MockLocationService extends IntentService {

    private final static String SERVICE_NAME = "Mock Location service";
    private static final float DEFAULT_ACCURACY = 1.f;

    public MockLocationService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Log.d("QQQQQ", "Change location to " + extras.getString("name"));
                // TODO: Send altitude as well
                setLocation(extras.getDouble("lat"), extras.getDouble("lon"), 0);
            }
        }
    }

    private void setLocation(double lat, double lon, double alt) {
        Location mockLocation = new Location(LocationManager.GPS_PROVIDER);
        mockLocation.setLatitude(lat);
        mockLocation.setLongitude(lon);
        mockLocation.setAltitude(alt);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setAccuracy(DEFAULT_ACCURACY);
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());

        LocationManager locationManager = ((MockLocationApplication) getApplication()).getLocationManager();
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mockLocation);
        locationManager.setTestProviderStatus(
                LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null,
                System.currentTimeMillis());
    }

}
