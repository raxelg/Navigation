package com.rtg.navigationwtd;

import java.util.ArrayList;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import expandListView.Adapter.ExpandableListAdapter;
import expandListView.Classes.*;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class Favorites extends AppCompatActivity {
    /**
     * Called when the activity is first created.
     */
    private ArrayList<ExpandListGroup> ExpListItems = new ArrayList<ExpandListGroup>();
    private ExpandableListAdapter ExpAdapter;
    private ExpandableListView ExpandList;
    private static final String TAG = "Favorites";
    private DatabaseHelper mDatabaseHelper;
    FloatingActionButton editBt;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        ExpandList = findViewById(R.id.list_of_favorites);
        mDatabaseHelper = new DatabaseHelper(this);
        updateExpandableListView();
        ExpAdapter = new ExpandableListAdapter(Favorites.this, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        editBt = findViewById(R.id.edit_fab);

        ExpandList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                String label = ExpAdapter.getGroup(groupPosition).getName();
                String address = ExpAdapter.getChild(groupPosition,0).getName();
                String coords = ExpAdapter.getChild(groupPosition,1).getName();

                Cursor data = mDatabaseHelper.getItemID(label);
                int groupID = -1;

                while(data.moveToNext()){
                    groupID = data.getInt(0);
                }
                if(groupID > -1){
                    Intent editScreenIntent = new Intent(Favorites.this, EditData.class);
                    editScreenIntent.putExtra("groupID",groupID);
                    editScreenIntent.putExtra("label",label);
                    editScreenIntent.putExtra("address",address);
                    editScreenIntent.putExtra("coords",coords);
                    editBt.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            startActivity(editScreenIntent);
                        }
                    });
                } else {
                    ToastMessage.message(getApplicationContext(),"No ID associated with name");
                }

                return false;
            }
        });

        editBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastMessage.message(getApplicationContext(),"Please select a location");
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.favorites_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.map:
                Intent map_intent = new Intent(Favorites.this, DispositivosBT.class);
                startActivity(map_intent);
                break;
            case R.id.add:
                Intent add_intent = new Intent(Favorites.this, LocationInput.class);
                startActivity(add_intent);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    private  void updateExpandableListView() {
        //get the data and modify group and children
        Cursor data = mDatabaseHelper.getData();

        if (data.getCount() != 0) {
            while (data.moveToNext()) {
                ArrayList<ExpandListChild> children = new ArrayList<ExpandListChild>();
                ExpandListGroup g = new ExpandListGroup(data.getString(1));
                ExpandListChild c1 = new ExpandListChild();
                ExpandListChild c2 = new ExpandListChild();

                c1.setName(data.getString(2));
                c1.setTag(data.getString(1));
                c2.setName(data.getString(3));
                c1.setTag(data.getString(1));
                children.add(c1);
                children.add(c2);
                g.setItems(children);
                ExpListItems.add(g);
            }
        }
    }
}