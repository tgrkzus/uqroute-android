package com.uqroute.uqroute;

// Java imports
import java.util.List;
import java.util.ArrayList;

// Android imports
import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.location.Location;
import android.util.Log;
import android.content.Intent;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

// Mapzen imports
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;
import com.mapzen.android.core.MapzenManager;
import com.mapzen.android.graphics.MapFragment;
import com.mapzen.android.graphics.MapzenMap;
import com.mapzen.android.graphics.OnMapReadyCallback;
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

// TODO split this class into a lot of other classes
public class MainActivity extends AppCompatActivity implements
        LostApiClient.ConnectionCallbacks {

    private MapzenMap map;
    private LostApiClient client;
    private MapzenRouter router;
    private Location currentLocation;
    private LngLat currentTarget;
    private boolean trackingLocation = true;
    private boolean queryingPermissions = false;
    private boolean routing = true;

    private static final int FINE_PERMISSION = 0;
    private static final int LOCATION_LIST_INTENT_CODE = 0;
    private static final String ACTIVITY_TAG = "MAP ACTIVITY";
    private static final String TAG = "LOST API";
    private static final String ROUTE_TAG = "ROUTING";

    LocationListener listener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Location: " + location);
            }
            set_location(location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Location provider disabled: " + provider);
            }
        }

        @Override
        public void onProviderEnabled(String provider) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Location provider enabled: " + provider);
            }
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
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route start");
            }
        }

        @Override
        public void onRecalculate(ValhallaLocation location) {
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route recalculate");
            }
            router.clearLocations();

            refresh_route();
        }

        @Override
        public void onSnapLocation(ValhallaLocation originalLocation, ValhallaLocation snapLocation) {
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route snap location");
            }
        }

        @Override
        public void onMilestoneReached(int index, RouteEngine.Milestone milestone) {
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route milestone reached");
            }
        }

        @Override
        public void onApproachInstruction(int index) {
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route approaching instruction");
            }
        }

        @Override
        public void onInstructionComplete(int index) {
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route instruction complete");
            }
        }

        @Override
        public void onUpdateDistance(int distanceToNextInstruction, int distanceToDestination) {
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route update distance");
            }
        }

        @Override
        public void onRouteComplete() {
            if (BuildConfig.DEBUG) {
                Log.d(ROUTE_TAG, "Route complete");
            }

            // Stop drawing
            map.clearRouteLine();
        }
    };
    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client2;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == LOCATION_LIST_INTENT_CODE) {
                //Log.d("TEST", "" + data.getExtras().getDoubleArray("NEW_LOCATION"));
                double[] info = data.getExtras().getDoubleArray("NEW_LOCATION");

                LngLat loc = new LngLat(info[1], info[0]);

                set_target(loc);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Setup content view
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        setSupportActionBar((Toolbar) findViewById(R.id.main_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.launcher_icon);

        // Setup location services
        initialize_location_services();

        onSearchRequested();

        // Setup api key
        MapzenManager.instance(this).setApiKey(BuildConfig.MAPZEN_KEY);

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
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2 = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void initialize_location_services() {
        if (trackingLocation) {
            if (getLocationPermissions()) {
                client = new LostApiClient.Builder(this).addConnectionCallbacks(this).build();
            }
        }
    }

    private void draw_route(Route r) {
        // Clean current route drawing
        map.clearRouteLine();

        // Generate latlng
        List<LngLat> list = new ArrayList<LngLat>();
        for (ValhallaLocation l : r.getGeometry()) {
            list.add(new LngLat(l.getLongitude(), l.getLatitude()));
        }
        map.drawRouteLine(list);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (trackingLocation) {
            if (getLocationPermissions()) {
                disconnect();
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.map_app_bar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                if (BuildConfig.DEBUG) {
                    Log.d(ACTIVITY_TAG, "Settings activated");
                }
                break;
            case R.id.action_target:
                if (BuildConfig.DEBUG) {
                    Log.d(ACTIVITY_TAG, "Target menu activated");
                }
                // Open location menu
                Intent i = new Intent(this, LocationListActivity.class);
                startActivityForResult(i, LOCATION_LIST_INTENT_CODE);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    protected boolean getLocationPermissions() {
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (permission == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
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
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_PERMISSION: {
                queryingPermissions = false;
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Granted
                    initialize_location_services();
                    connect();
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
        } catch (SecurityException e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Security exception when fetching location" + e.getMessage());
            }
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
                if (getLocationPermissions()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Location services connecting");
                    }
                    client.connect();
                    map.setMyLocationEnabled(true);
                }
            }
        }
    }

    private void disconnect() {
        if (map != null) {
            if (trackingLocation) {
                if (getLocationPermissions()) {
                    if (BuildConfig.DEBUG) {
                        Log.d(TAG, "Location services disconnecting");
                    }
                    client.disconnect();
                    map.setMyLocationEnabled(false);
                }
            }
        }
    }

    private void refresh_route() {
        router.clearLocations();
        router.setLocation(new double[]{currentLocation.getLatitude(), currentLocation.getLongitude()});
        router.setLocation(new double[]{currentTarget.latitude, currentTarget.longitude});

        router.fetch();
    }

    private void set_target(LngLat p) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "New target set: " + p.latitude + ", " + p.longitude);
        }

        currentTarget = p;
        refresh_route();
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
                        if (BuildConfig.DEBUG) {
                            Log.d("ROUTING", "Successfully routed");
                        }
                        routeEngine.setRoute(route);

                        draw_route(route);
                    }

                    @Override
                    public void failure(int i) {
                        Log.d("ROUTING ERROR", "Failed to route, error: " + i);
                    }
                });

                double[] start = {l.getLatitude(), l.getLongitude()};
                double[] end = {-27.49668, 153.010411};
                router.setLocation(start);
                router.setLocation(end);
                router.fetch();
            } else {
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

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client2.connect();
        AppIndex.AppIndexApi.start(client2, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client2, getIndexApiAction());
        client2.disconnect();
    }
}
