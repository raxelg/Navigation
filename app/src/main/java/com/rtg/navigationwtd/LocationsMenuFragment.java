package com.rtg.navigationwtd;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import androidx.fragment.app.ListFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LocationsMenuFragment extends ListFragment {

    private static final String TAG = "LocationsFragment";
    DatabaseHelper mDatabaseHelper;
    OnDataPass dataPasser;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup parent = (ViewGroup) inflater.inflate(android.R.layout.simple_list_item_2,container,false);
        mDatabaseHelper = new DatabaseHelper(getContext());
        updateListView();
        parent.addView(view,0);
        return parent;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        String text = l.getItemAtPosition(position).toString();
        String[] parsedLabel = text.split("=");
        String address = parsedLabel[1];
        Log.d(TAG, "onItemClick: You Clicked on " + parsedLabel[1]);

        String coords = GeoCoder.getLocationFromAddress(address);

        if(coords != null){
            passData(coords);                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                            
        }
    }


    private void updateListView() {
        Log.d(TAG, "populateListView: Displaying data in the ListView.");

        //get the data and append to a list
        Cursor data = mDatabaseHelper.getData();
        ArrayList<Map<String,String>> locations = new ArrayList<Map<String, String>>();

        if (data.getCount() != 0) {
            while (data.moveToNext()) {
                Map<String,String> new_loc = new HashMap<String,String>();
                new_loc.put("Label", data.getString(1));
                new_loc.put("Address", data.getString(2));
                locations.add(new_loc);
            }
        }

        SimpleAdapter simpleAdapter = new SimpleAdapter(getActivity(),locations,android.R.layout.simple_list_item_2,
                new String[]{"Label","Address"},new int[]{android.R.id.text1,android.R.id.text2});

        setListAdapter(simpleAdapter);
    }

    public interface OnDataPass {
        public void onDataPass(String data);
    }

    public void passData(String data){
        dataPasser.onDataPass(data);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        dataPasser = (OnDataPass) context;
    }
}
