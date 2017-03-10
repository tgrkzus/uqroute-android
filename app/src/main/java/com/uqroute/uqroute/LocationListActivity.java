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

// Java Imports
import java.util.List;

public class LocationListActivity extends ListActivity {
    static private final String TAG = "LOCATION_LIST";

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        Log.d(TAG, "Item " + id + " clicked: " + l.getItemAtPosition(position));

        // Reroute to new location
        Intent i = new Intent();
        double[] data;
        if (id == 0) {
            data = new double[] {-27.496361, 153.014117};
        }
        else {
            data = new double[] {-27.49668, 153.010411};
        }
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
        String[] values = new String[] {
                "Item 1",
                "Item 2",
                "Item 3",
                "Item 4",
                "Item 5",
                "Item 6"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, values);

        setListAdapter(adapter);
    }
}
