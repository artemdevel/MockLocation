package com.github.artemdevel.mocklocation;

import android.app.IntentService;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;

public final class MockLocationService extends IntentService {

    private final static String SERVICE_NAME = "Mock Location service";

    public MockLocationService() {
        super(SERVICE_NAME);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                LocationManager locationManager = ((MockLocationApplication) getApplication()).getLocationManager();
                MockLocationUtils.startProvider(locationManager);
                MockLocationUtils.setLocation(locationManager,
                        MockLocationUtils.buildLocation(
                                extras.getDouble("lat"),
                                extras.getDouble("lon"),
                                extras.getDouble("alt")));
            }
        }
    }

}
