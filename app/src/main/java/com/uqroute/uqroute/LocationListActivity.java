package com.uqroute.uqroute;

// Android Imports
import android.app.ListActivity;
import android.app.LoaderManager;
import android.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.app.SearchManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.database.Cursor;

// Mapzen Imports
import com.mapzen.tangram.LngLat;

// Java Imports
import java.util.List;
import java.util.ArrayList;

class Location {
    public Location(String name, double latitude, double longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    String name;
    double latitude;
    double longitude;
}

public class LocationListActivity extends ListActivity {
    static private final String TAG = "LOCATION_LIST";

    static private final Location[] LocList = new Location[] {
            new Location("Hawken Engineering Building", -27.499968, 153.013774),
            new Location("Forgen Smith", -27.496928, 153.013072)
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Log.d(TAG, "Item " + id + " clicked: " + l.getItemAtPosition(position));

        // Reroute to new location
        Intent i = new Intent();
        double[] data;
        data = new double[] {LocList[(int) id].latitude, LocList[(int) id].longitude};
        i.putExtra("NEW_LOCATION", data);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        Log.d(TAG, "Location list initialized");

       // ListView l = (ListView) this.findViewById(R.id.st);

        // Populate list
        List<String> values = new ArrayList<>();
        for (Location b : LocList) {
            values.add(b.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);

        setListAdapter(adapter);
    }
}
