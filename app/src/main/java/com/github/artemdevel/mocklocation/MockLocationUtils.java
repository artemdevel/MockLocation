package com.github.artemdevel.mocklocation;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.SystemClock;
import android.support.annotation.NonNull;

public final class MockLocationUtils {

    private static final float DEFAULT_ACCURACY = 1.f;

    private MockLocationUtils() {

    }

    public static void startProvider(@NonNull LocationManager locationManager) {
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

    public static void stopProvider(@NonNull LocationManager locationManager) {
        locationManager.clearTestProviderLocation(LocationManager.GPS_PROVIDER);
        locationManager.clearTestProviderEnabled(LocationManager.GPS_PROVIDER);
        locationManager.removeTestProvider(LocationManager.GPS_PROVIDER);
    }

    public static void setLocation(@NonNull LocationManager locationManager, @NonNull Location location) {
        locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, location);
        locationManager.setTestProviderStatus(
                LocationManager.GPS_PROVIDER,
                LocationProvider.AVAILABLE,
                null,
                System.currentTimeMillis());
    }

    public static Location buildLocation(double lat, double lon, double alt) {
        Location location = new Location(LocationManager.GPS_PROVIDER);
        location.setLatitude(lat);
        location.setLongitude(lon);
        location.setAltitude(alt);
        location.setTime(System.currentTimeMillis());
        location.setAccuracy(DEFAULT_ACCURACY);
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        return location;
    }

}
