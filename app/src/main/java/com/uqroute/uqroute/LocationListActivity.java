package com.uqroute.uqroute;

// Android Imports
import android.app.ListActivity;
        import android.os.Bundle;
import android.content.Intent;
        import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

// Mapzen Imports

// Java Imports
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

class Location {
    public Location(String name, String buildingNum, double latitude, double longitude) {
        this.name = name;
        this.buildingNum = buildingNum;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    String name;
    String buildingNum;
    double latitude;
    double longitude;
}

public class LocationListActivity extends ListActivity {
    static private final String TAG = "LOCATION_LIST";
    static private List<Location> locations;

    static private Comparator<Location> sortByName = new Comparator<Location>() {
        @Override
        public int compare(Location lhs, Location rhs) {
            return lhs.name.compareToIgnoreCase(rhs.name);
        }
    };

    static private Comparator<Location> sortByBuildingNumber = new Comparator<Location>() {
        @Override
        public int compare(Location lhs, Location rhs) {
            int order = 0;
            try {
                int l = Integer.parseInt(lhs.buildingNum);
                int r = Integer.parseInt(rhs.buildingNum);
                order = l < r ? -1 : l == r ? 0 : 1;
            }
            catch (NumberFormatException e) {
                // Then one/both of building numbers has a letter in it
                // Thanks magic regex: https://stackoverflow.com/questions/10372862/java-string-remove-all-non-numeric-characters
                String l = lhs.buildingNum.replaceAll("[^\\d.]", "");
                String r = rhs.buildingNum.replaceAll("[^\\d.]", "");

                // Just the number
                int lInt = Integer.parseInt(l);
                int rInt = Integer.parseInt(r);

                // Just the letters (if they exist)
                String lLetter = lhs.buildingNum.replace(l, "");
                String rLetter = rhs.buildingNum.replace(r, "");


                if (l != lhs.buildingNum && r != rhs.buildingNum) {
                    // Both have letters

                    if (lInt != rInt) {
                        order = lInt < rInt ? -1 : lInt == rInt ? 0 : 1;
                    }
                    else {
                        // Same numbers we have to sort by letters
                        order = lLetter.compareToIgnoreCase(rLetter);
                    }
                }
                else {
                    order = lInt < rInt ? -1 : lInt == rInt ? 0 : 1;
                }
            }

            return order;
        }
    };

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Item " + id + " clicked: " + l.getItemAtPosition(position));
        }

        // Reroute to new location
        Intent i = new Intent();
        double[] data;
        data = new double[] {locations.get((int) id).latitude, locations.get((int) id).longitude};
        i.putExtra("NEW_LOCATION", data);
        setResult(RESULT_OK, i);
        finish();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Location list initialized");
        }

        // ListView l = (ListView) this.findViewById(R.id.st);

        // Populate list (TODO error handling better)
        locations = fetch_json();
        List<String> values = new ArrayList<>();
        for (Location b : locations) {
            values.add(b.buildingNum + " | " + b.name);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);

        setListAdapter(adapter);
    }

    private ArrayList<Location> fetch_json() {
        String json;
        try {
            InputStream is = getAssets().open("location.cache");

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        }
        catch (IOException e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Location JSON unsuccessfully fetched");
                e.printStackTrace();
            }
            return null;
        }
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Location JSON fetched");
        }
        ArrayList<Location> l = new ArrayList<>();

        try {
            JSONObject o = new JSONObject(json);
            Iterator<?> keys = o.keys();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (o.get(key) instanceof JSONObject) {
                    double latitude = (double) ((JSONObject) o.get(key)).get("latitude");
                    double longitude = (double) ((JSONObject) o.get(key)).get("longitude");
                    String name = (String) ((JSONObject) o.get(key)).get("title");
                    l.add(new Location(name, key, latitude, longitude));
                }
            }
        }
        catch (JSONException e) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "JSON parsing failure");
                e.printStackTrace();
            }
            return null;
        }

        // Sort list of locations alphabetically

        Collections.sort(l, sortByBuildingNumber);

        return l;
    }
}
