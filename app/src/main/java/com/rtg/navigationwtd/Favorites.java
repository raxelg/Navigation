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
    private android.widget.ExpandableListAdapter ExpAdapter;
    private ExpandableListView ExpandList;
    private static final String TAG = "Favorites";
    private DatabaseHelper mDatabaseHelper;
    FloatingActionButton editBt;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favorites);
        editBt = findViewById(R.id.edit_fab);
        ExpandList = findViewById(R.id.list_of_favorites);
        mDatabaseHelper = new DatabaseHelper(this);
        updateExpandableListView();
        ExpAdapter = new ExpandableListAdapter(Favorites.this, ExpListItems);
        ExpandList.setAdapter(ExpAdapter);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

        editBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Favorites.this, LocationInput.class);
                startActivity(intent);
            }
        });

        ExpandList.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
            @Override
            public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                android.widget.ExpandableListAdapter adapter = parent.getExpandableListAdapter();
                String label = adapter.getGroup(groupPosition).toString();
                String address = adapter.getChild(groupPosition,0).toString();
                String coords = adapter.getChild(groupPosition,1).toString();

                Cursor data = mDatabaseHelper.getItemID(label);
                int itemID = -1;

                while(data.moveToNext()){
                    itemID = data.getInt(0);
                }
                if(itemID > -1){
                    Intent editScreenIntent = new Intent(Favorites.this, EditData.class);
                    editScreenIntent.putExtra("id",itemID);
                    editScreenIntent.putExtra("label",label);
                    editScreenIntent.putExtra("address",address);
                    editScreenIntent.putExtra("coords",coords);
                    startActivity(editScreenIntent);
                } else {
                    ToastMessage.message(getApplicationContext(),"No ID associated with name");
                }

                return true;
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
                ExpandListGroup g = new ExpandListGroup();
                ExpandListChild c1 = new ExpandListChild();
                ExpandListChild c2 = new ExpandListChild();

                g.setName(data.getString(1));
                c1.setName(data.getString(2));
                c2.setName(data.getString(3));
                children.add(c1);
                children.add(c2);
                g.setItems(children);
                ExpListItems.add(g);
            }
        }
    }
}