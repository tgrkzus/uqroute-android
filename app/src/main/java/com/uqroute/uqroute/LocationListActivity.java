package com.uqroute.uqroute;
import com.uqroute.uqroute.LocationListAdapter;

// Android Imports
import android.app.ListActivity;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.PopupMenu;

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

public class LocationListActivity extends AppCompatActivity { 
    private RecyclerView rv;
    private List<Location> locations;
    private Comparator<Location> sortType;

    static private final String TAG = "LOCATION_LIST";

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


    private PopupMenu.OnMenuItemClickListener menuListener = new PopupMenu.OnMenuItemClickListener() {
        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch(item.getItemId()) {
                case R.id.sort_by_name:
                    sortType = sortByName;
                    refresh_location_list();
                    return true;
                case R.id.sort_by_building_number:
                    sortType = sortByBuildingNumber;
                    refresh_location_list();
                    return true;
                default:
                    return false;
            }
        }
    };

    /*
    private RecyclerView.On itemClickListener = new ListView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> l, View v, int position, long id) {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Item " + id + " clicked: " + l.getItemAtPosition(position));
            }

            // Reroute to new location
            Intent i = new Intent();
            double[] data;
            data = new double[]{locations.get((int) id).latitude, locations.get((int) id).longitude};
            i.putExtra("NEW_LOCATION", data);
            setResult(RESULT_OK, i);
            finish();
        }
    };
    */

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        setSupportActionBar((Toolbar) findViewById(R.id.location_toolbar));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayUseLogoEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.launcher_icon);

        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Location list initialized");
        }

        sortType = sortByBuildingNumber;
        refresh_location_list();
        //rv.setOnClickListener(itemClickListener);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.location_list_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_settings:
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Settings activated");
                }
                break;
            case R.id.action_sorting_options:
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Sorting menu activated");
                }
                // Open sorting menu
                PopupMenu popup = new PopupMenu(this, findViewById(R.id.action_sorting_options));
                MenuInflater inflater = popup.getMenuInflater();
                inflater.inflate(R.menu.sorting_popup_menu, popup.getMenu());
                popup.setOnMenuItemClickListener(menuListener);
                popup.show();
                break;
        }

        return super.onOptionsItemSelected(item);
    }


    private void refresh_location_list() {
        // Setup list
        rv = (RecyclerView) this.findViewById(R.id.location_list_view);

        // Populate list (TODO error handling better)
        if (locations == null) {
            locations = fetch_json();
        }

        // Sort list of locations
        Collections.sort(locations, sortType);
        LocationListAdapter adapter = new LocationListAdapter(locations);

        // Linear layout
        RecyclerView.LayoutManager layout = new LinearLayoutManager(this);
        rv.setLayoutManager(layout);

        rv.setAdapter(adapter);
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
        return l;
    }
}
