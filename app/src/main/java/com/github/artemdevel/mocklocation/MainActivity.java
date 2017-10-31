package com.github.artemdevel.mocklocation;

import android.app.AppOpsManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String PREF_LAT = "lat";
    private static final String PREF_LON = "lon";
    private static final String PREF_ALT = "alt";
    private static final String PREF_LOC = "loc";
    private static final String PREF_LOC_PTR = "loc_ptr";
    private static final String CHANNEL_ID = "recent_locations";
    private static final String CHANNEL_NAME = "Recent Location";
    private static final int LOC_LIMIT = 5; // up to 5 recent locations could be stored
    private static final int PLACE_PICKER_REQUEST = 100;

    private final static LatLngBounds EU_BOUNDS = new LatLngBounds(new LatLng(27.6363, -31.2660), new LatLng(81.0087, 39.8693));
    private final static LatLngBounds NA_BOUNDS = new LatLngBounds(new LatLng(24.9493, -125.0011), new LatLng(49.5904, -66.9326));
    private final static LatLngBounds AS_BOUNDS = new LatLngBounds(new LatLng(-12.5611, 19.6381), new LatLng(82.5005, 180.0000));
    private final static LatLngBounds AF_BOUNDS = new LatLngBounds(new LatLng(-46.900, -25.3587), new LatLng(37.5671, 63.5254));
    private final static LatLngBounds SA_BOUNDS = new LatLngBounds(new LatLng(-59.4505, -109.4749), new LatLng(13.3903, -26.3325));
    private final static LatLngBounds AU_BOUNDS = new LatLngBounds(new LatLng(-53.0587, 105.3770), new LatLng(-6.0694, -175.2925));

    private final static String REGION_EUROPE = "Europe";
    private final static String REGION_NORTH_AMERICA = "North America";
    private final static String REGION_ASIA = "Asia";
    private final static String REGION_AFRICA = "Africa";
    private final static String REGION_SOUTH_AMERICA = "South America";
    private final static String REGION_AUSTRALIA = "Australia";

    private final static String[] MAP_REGIONS = new String[]{
            REGION_EUROPE, REGION_NORTH_AMERICA, REGION_ASIA, REGION_AFRICA, REGION_SOUTH_AMERICA, REGION_AUSTRALIA
    };
    private final static Map<String, LatLngBounds> REGIONS_BOUNDS = new HashMap<String, LatLngBounds>() {{
        put(REGION_EUROPE, EU_BOUNDS);
        put(REGION_NORTH_AMERICA, NA_BOUNDS);
        put(REGION_ASIA, AS_BOUNDS);
        put(REGION_AFRICA, AF_BOUNDS);
        put(REGION_SOUTH_AMERICA, SA_BOUNDS);
        put(REGION_AUSTRALIA, AU_BOUNDS);
    }};

    private EditText latText;
    private EditText lonText;
    private EditText altText;
    private Button startProvider;
    private Button stopProvider;
    private Button openMap;
    private Button recentLocation;
    private TextView status;
    private MockLocationProvider provider;
    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkMockLocationSetting();
        Intent intent = getIntent();
        if (intent != null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                Log.d("QQQQQ", "onCreate: " + extras.getString("name"));
            } else {
                Log.d("QQQQQ", "onCreate: No extras");
            }
        } else {
            Log.d("QQQQQ", "onCreate: No intent");
        }

        preferences = getPreferences(Context.MODE_PRIVATE);

        latText = findViewById(R.id.lat_text);
        latText.setText(preferences.getString(PREF_LAT, "0"));

        lonText = findViewById(R.id.lon_text);
        lonText.setText(preferences.getString(PREF_LON, "0"));

        altText = findViewById(R.id.alt_text);
        altText.setText(preferences.getString(PREF_ALT, "0"));

        startProvider = findViewById(R.id.provider_start);
        startProvider.setOnClickListener(this);

        stopProvider = findViewById(R.id.provider_stop);
        stopProvider.setOnClickListener(this);
        stopProvider.setClickable(false);

        openMap = findViewById(R.id.open_map);
        openMap.setOnClickListener(this);

        recentLocation = findViewById(R.id.recent_location);
        recentLocation.setOnClickListener(this);

        status = findViewById(R.id.service_status_text);

        provider = new MockLocationProvider(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.provider_start:
                disableButtons();

                double lat = Double.parseDouble(latText.getText().toString());
                double lon = Double.parseDouble(lonText.getText().toString());
                double alt = Double.parseDouble(altText.getText().toString());

                preferences.edit().putString(PREF_LAT, latText.getText().toString()).apply();
                preferences.edit().putString(PREF_LON, lonText.getText().toString()).apply();
                preferences.edit().putString(PREF_ALT, altText.getText().toString()).apply();

                try {
                    // Provider must be started before any mock locations set
                    provider.startProvider();
                    provider.setLocation(lat, lon, alt);
                } catch (SecurityException e) {
                    enableButtons();
                    showAlertDialog();
                }

                showRecentLocationNotifications();
                break;

            case R.id.provider_stop:
                enableButtons();
                provider.stopProvider();
                break;

            case R.id.open_map:
                showMapRegionSelectionDialog();
                break;

            case R.id.recent_location:
                showRecentLocationDialog();
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_PICKER_REQUEST:
                if (resultCode == RESULT_OK) {
                    Place place = PlacePicker.getPlace(this, data);
                    latText.setText(String.format(Locale.US, "%f", place.getLatLng().latitude));
                    lonText.setText(String.format(Locale.US, "%f", place.getLatLng().longitude));
                    storeLocation(place);
                }
                break;
        }
    }

    private void checkMockLocationSetting() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                AppOpsManager opsManager = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
                if (opsManager != null) {
                    opsManager.checkOp(AppOpsManager.OPSTR_MOCK_LOCATION, android.os.Process.myUid(), BuildConfig.APPLICATION_ID);
                }
            } catch (RuntimeException ex) {
                showAlertDialog();
            }
        } else {
            if (Settings.Secure.getString(getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).equals("0")) {
                showAlertDialog();
            }
        }
    }

    private void enableButtons() {
        startProvider.setClickable(true);
        startProvider.setTextColor(getResources().getColor(R.color.black));

        openMap.setClickable(true);
        openMap.setTextColor(getResources().getColor(R.color.black));

        recentLocation.setClickable(true);
        recentLocation.setTextColor(getResources().getColor(R.color.black));

        stopProvider.setClickable(false);
        stopProvider.setTextColor(getResources().getColor(R.color.grey));

        status.setText(R.string.service_disabled);
        status.setTextColor(getResources().getColor(R.color.red));
    }

    private void disableButtons() {
        startProvider.setClickable(false);
        startProvider.setTextColor(getResources().getColor(R.color.grey));

        openMap.setClickable(false);
        openMap.setTextColor(getResources().getColor(R.color.grey));

        recentLocation.setClickable(false);
        recentLocation.setTextColor(getResources().getColor(R.color.grey));

        stopProvider.setClickable(true);
        stopProvider.setTextColor(getResources().getColor(R.color.black));

        status.setText(R.string.service_enabled);
        status.setTextColor(getResources().getColor(R.color.green));
    }

    private void showAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.mock_locations_alert)
                .setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivityForResult(new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS), 0);
                    }
                })
                .setNegativeButton(R.string.alert_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create().show();
    }

    private void showRecentLocationDialog() {
        List<String> recentLocations = new ArrayList<>();
        for (int i = 0; i < LOC_LIMIT; i++) {
            String pref_loc = String.format(Locale.US, "%s%d", PREF_LOC, i);
            String location = preferences.getString(pref_loc, null);
            if (location != null) {
                String name = location.split(",")[0];
                recentLocations.add(name);
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.recent_locations_title);
        if (recentLocations.size() > 0) {
            builder.setItems(recentLocations.toArray(new String[0]), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pref_loc = String.format(Locale.US, "%s%d", PREF_LOC, which);
                    String location = preferences.getString(pref_loc, null);
                    if (location != null) {
                        String[] parts = location.split(",");
                        latText.setText(parts[1]);
                        lonText.setText(parts[2]);
                    }
                }
            });
        } else {
            builder.setMessage(R.string.no_recent_locations_message)
                    .setPositiveButton(R.string.alert_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
        }
        builder.create().show();
    }

    private void showMapRegionSelectionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.map_region_selection_title)
                .setItems(MAP_REGIONS, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
                            builder.setLatLngBounds(REGIONS_BOUNDS.get(MAP_REGIONS[which]));
                            startActivityForResult(builder.build(MainActivity.this), PLACE_PICKER_REQUEST);
                        } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException e) {
                            GoogleApiAvailability api = GoogleApiAvailability.getInstance();
                            api.getErrorDialog(MainActivity.this, api.isGooglePlayServicesAvailable(MainActivity.this), PLACE_PICKER_REQUEST);
                        }
                    }
                }).create().show();
    }

    private void storeLocation(Place place) {
        int loc_ptr = preferences.getInt(PREF_LOC_PTR, 0);
        String pref_loc = String.format(Locale.US, "%s%d", PREF_LOC, loc_ptr);
        String location = String.format(Locale.US, "%s,%f,%f", place.getName(), place.getLatLng().latitude, place.getLatLng().longitude);
        preferences.edit().putString(pref_loc, location).apply();
        loc_ptr += 1;
        if (loc_ptr == LOC_LIMIT) {
            loc_ptr = 0;
        }
        preferences.edit().putInt(PREF_LOC_PTR, loc_ptr).apply();
    }

    private void showRecentLocationNotifications() {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            for (int i = 0; i < LOC_LIMIT; i++) {
                String pref_loc = String.format(Locale.US, "%s%d", PREF_LOC, i);
                String location = preferences.getString(pref_loc, null);
                if (location != null) {
                    String locationName = location.split(",")[0];
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.putExtra("name", locationName);
                    PendingIntent pendingIntent = PendingIntent.getActivity(this, i + 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                            .setSmallIcon(R.drawable.ic_place_white_24dp)
                            .setContentTitle(CHANNEL_NAME)
                            .setContentText(locationName)
                            .setContentIntent(pendingIntent)
                            .setOngoing(true)
//                            .setAutoCancel(true)
                            .build();
                    manager.notify(i + 1, notification);
                }
            }
        }
    }

}
