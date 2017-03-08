package com.uqroute.uqroute;

import android.app.ListActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.app.SearchManager;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import java.util.List;

public class LocationListActivity extends AppCompatActivity {
    static private final String TAG = "LOCATION_LIST";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_list);
        Log.d(TAG, "Location list initialized");


    }
}
