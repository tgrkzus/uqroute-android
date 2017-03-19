package com.uqroute.uqroute;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LocationListAdapter extends RecyclerView.Adapter<LocationListAdapter.ViewHolder> {
    private List<Location> data;

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView name;
        public TextView buildingNumber;

        public ViewHolder(View v) {
            super(v);
            this.name = (TextView) v.findViewById(R.id.location_list_name);
            this.buildingNumber = (TextView) v.findViewById(R.id.location_list_num);
            v.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int id = getLayoutPosition();
            if (BuildConfig.DEBUG) {
                Log.d("LOCATION_LIST_ADAPTER", "Item " + id + " clicked: " + name.getText() + " : " + buildingNumber.getText());
            }
            LocationListActivity act = (LocationListActivity) v.getContext();
            act.locationClick(id);
        }
    }

    public LocationListAdapter(List<Location> data) {
        this.data = new ArrayList<>();
        this.data = data;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.location_list_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.name.setText(data.get(position).name);
        holder.buildingNumber.setText(data.get(position).buildingNum);

        holder.name.setGravity(Gravity.CENTER_HORIZONTAL);
        holder.buildingNumber.setGravity(Gravity.CENTER_HORIZONTAL);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }
}
