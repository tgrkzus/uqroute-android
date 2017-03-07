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
import com.mapzen.helpers.RouteEngine;
import com.mapzen.helpers.RouteListener;
import com.mapzen.model.ValhallaLocation;
import com.mapzen.tangram.LngLat;
import com.mapzen.android.lost.api.LostApiClient;
import com.mapzen.android.lost.api.LocationRequest;
import com.mapzen.android.lost.api.LocationListener;
import com.mapzen.android.lost.api.LocationServices;
import com.mapzen.android.routing.MapzenRouter;
import com.mapzen.valhalla.Route;
import com.mapzen.valhalla.RouteCallback;

import org.jetbrains.annotations.NotNull;

public class MainActivity extends AppCompatActivity implements
        LostApiClient.ConnectionCallbacks {

    private MapzenMap map;
    private LostApiClient client;
    private MapzenRouter router;
    private Location currentLocation;
    private boolean trackingLocation = true;
    private boolean queryingPermissions = false;
    private boolean routing = true;
    static final int FINE_PERMISSION = 0;
    private static final String TAG = "LOST API";
    private static final String ROUTE_TAG = "ROUTING";

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            Log.d(TAG, "Location: " + location);
            set_location(location);
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

    // Route engine
    private RouteEngine routeEngine = new RouteEngine() {
        @Override
        public void onLocationChanged(ValhallaLocation location) {
            super.onLocationChanged(location);
        }

        @Override
        public void setRoute(Route route) {
            super.setRoute(route);
        }

        @Override
        public Route getRoute() {
            return super.getRoute();
        }

        @Override
        public void setListener(RouteListener listener) {
            super.setListener(listener);
        }
    };

    // Route listener
    private RouteListener routeListener = new RouteListener() {
        @Override
        public void onRouteStart() {
            Log.d(ROUTE_TAG, "Route start");
        }

        @Override
        public void onRecalculate(ValhallaLocation location) {
            Log.d(ROUTE_TAG, "Route recalculate");
            router.clearLocations();

            double[] start = {currentLocation.getLatitude(), currentLocation.getLongitude()};
            double[] end = {-27.49668, 153.010411};
            router.setLocation(start);
            router.setLocation(end);
            router.fetch();
        }

        @Override
        public void onSnapLocation(ValhallaLocation originalLocation, ValhallaLocation snapLocation) {
            Log.d(ROUTE_TAG, "Route snap location");
        }

        @Override
        public void onMilestoneReached(int index, RouteEngine.Milestone milestone) {
            Log.d(ROUTE_TAG, "Route milestone reached");
        }

        @Override
        public void onApproachInstruction(int index) {
            Log.d(ROUTE_TAG, "Route approaching instruction");
        }

        @Override
        public void onInstructionComplete(int index) {
            Log.d(ROUTE_TAG, "Route instruction complete");
        }

        @Override
        public void onUpdateDistance(int distanceToNextInstruction, int distanceToDestination) {
            Log.d(ROUTE_TAG, "Route update distance");
        }

        @Override
        public void onRouteComplete() {
            Log.d(ROUTE_TAG, "Route complete");
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

                // Setup router
                routeEngine.setListener(routeListener);

                // Connect to location services
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
        Location l = null;
        try {
            LocationServices.FusedLocationApi.requestLocationUpdates(client, request, listener);

            // Get current location
            l = LocationServices.FusedLocationApi.getLastLocation(client);
        }
        catch (SecurityException e) {
            Log.d(TAG, "Security exception when fetching location" + e.getMessage());
        }

        if (l != null) {
            // Set new location
            set_location(l);
        }
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

    private void set_location(Location l) {
        currentLocation = l;
        if (map != null && routing) {
            // Setup/Update routing
            if (router == null) {
                router = new MapzenRouter(this);
                router.setWalking();
                router.setCallback(new RouteCallback() {
                    @Override
                    public void success(Route route) {
                        Log.d("ROUTING", "Successfully routed");
                        routeEngine.setRoute(route);
                        // Generate latlng
                        List<LngLat> list = new ArrayList<LngLat>();
                        for (ValhallaLocation l : route.getGeometry()){
                            list.add(new LngLat(l.getLongitude(), l.getLatitude()));
                        }
                        map.drawRouteLine(list);
                    }

                    @Override
                    public void failure(int i) {
                        Log.d("ROUTING", "Failed to route, error: " + i);
                    }
                });

                double[] start = {l.getLatitude(), l.getLongitude()};
                double[] end = {-27.49668, 153.010411};
                router.setLocation(start);
                router.setLocation(end);
                router.fetch();
            }
            else {
                if (routeEngine.getRoute() != null) {
                    ValhallaLocation loc = new ValhallaLocation();
                    loc.setBearing(l.getBearing());
                    loc.setLatitude(l.getLatitude());
                    loc.setLongitude(l.getLongitude());
                    routeEngine.onLocationChanged(loc);
                }
            }
        }
    }
}
