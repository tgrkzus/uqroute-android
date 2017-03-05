package com.uqroute.uqroute;

// Java imports
import java.util.List;
import java.util.ArrayList;

// Android imports
import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.util.Log;
import android.view.Window;

// Mapzen imports
import com.mapzen.android.core.MapzenManager;
import com.mapzen.android.graphics.MapFragment;
import com.mapzen.android.graphics.MapzenMap;
import com.mapzen.android.graphics.OnMapReadyCallback;
import com.mapzen.android.graphics.model.Marker;
import com.mapzen.android.graphics.model.Polygon;
import com.mapzen.tangram.LngLat;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationServices;

public class MainActivity extends AppCompatActivity implements
        LostApiClient.ConnectionCallbacks {

    private MapzenMap map;
    private LostApiClient client;
    private boolean trackingLocation = true;
    private boolean queryingPermissions = false;
    static final int FINE_PERMISSION = 0;
    private static final String TAG = "LOST API";

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location: " + location);
            set_location(new LngLat(location.getLongitude(), location.getLatitude()));
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "Location provider disabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "Location provider enabled: " + provider);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup content view
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        // Setup location services
        if (trackingLocation) {
            if (getLocationPermissions()) {
                client = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();
            }
        }

        // Setup api key
        MapzenManager.instance(this).setApiKey("mapzen-RH6Bt1B");

        // Setup map
        MapFragment mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(MapzenMap map) {
                MainActivity.this.map = map;
                map.setPosition(new LngLat(153.0147179, -27.4977877));
                map.setRotation(0f);
                map.setZoom(15f);
                map.setTilt(0f);

                connect();
            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (trackingLocation) {
            disconnect();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (trackingLocation) {
            if (getLocationPermissions()) {
                connect();
            }
        }
    }

    protected boolean getLocationPermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        else {
            if (!queryingPermissions) {
                queryingPermissions = true;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_PERMISSION);
            }
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_PERMISSION: {
                queryingPermissions = false;
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Granted
                } else {
                    // Denied
                }
            }
        }
    }

    @Override
    public void onConnected() {
        // Called when location services are connected
        LocationRequest request = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
                .setInterval(1000)
                .setSmallestDisplacement(10);

        LocationServices.FusedLocationApi.requestLocationUpdates(client, request, listener);

        // Get current location
        Location l = LocationServices.FusedLocationApi.getLastLocation(client);

        set_location(new LngLat(l.getLongitude(), l.getLatitude()));
    }

    @Override
    public void onConnectionSuspended() {

    }

    private void connect() {
        if (map != null) {
            if (trackingLocation) {
                Log.d(TAG, "Location services connecting");
                client.connect();
                map.setMyLocationEnabled(true);
            }
        }
    }

    private void disconnect() {
        if (map != null) {
            if (trackingLocation) {
                Log.d(TAG, "Location services disconnecting");
                client.disconnect();
                map.setMyLocationEnabled(false);
            }
        }
    }

    private void set_location(LngLat l) {
        if (map != null) {

        }
    }
}
