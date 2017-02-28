package com.uqroute.uqroute;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.mapzen.android.graphics.MapFragment;
import com.mapzen.android.graphics.MapzenMap;
import com.mapzen.android.graphics.OnMapReadyCallback;

public class MainActivity extends AppCompatActivity {


    private MapzenMap map;
    private boolean enableLocationOnResume = false;
    static final int FINE_PERMISSION = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            public void onMapReady(MapzenMap map) {
                MainActivity.this.map = map;

                if (getLocationPermissions()) {
                    map.setMyLocationEnabled(true);
                }
            }
        });
    }

    @Override protected void onPause() {
        super.onPause();
        if (getLocationPermissions()) {
            if (map.isMyLocationEnabled()) {
                map.setMyLocationEnabled(false);
                enableLocationOnResume = true;
            }
        }
    }

    @Override protected void onResume() {
        super.onResume();
        if (getLocationPermissions()) {
            if (enableLocationOnResume) {
                map.setMyLocationEnabled(true);
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
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    FINE_PERMISSION);
            return false;
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case FINE_PERMISSION: {
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
}
