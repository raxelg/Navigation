package com.rtg.navigationwtd;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.widget.SimpleCursorAdapter;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rtg.MyApp;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocationInput extends AppCompatActivity {
    private DatabaseHelper mDatabaseHelper;
    public SimpleCursorAdapter eladapter;
    FloatingActionButton add_location, view_data;
    TextView label_title;
    EditText label_input, address_input, coords_input;
    String coords, selectedCoords, selectedAddress;

    Geocoder geocoder = new Geocoder(MyApp.getContext(), Locale.getDefault());
    List<Address> addresses = new ArrayList();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        setContentView(R.layout.add_new_location);

        add_location = findViewById(R.id.add_button);
        view_data = findViewById(R.id.data_button);
        label_input = findViewById(R.id.label_input);
        address_input = findViewById(R.id.address_input);
        coords_input = findViewById(R.id.coords_input);
        mDatabaseHelper = new DatabaseHelper(this);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);


        Intent receivedIntent = getIntent();
        selectedAddress = receivedIntent.getStringExtra("address");
        selectedCoords = receivedIntent.getStringExtra("coords");

        if(selectedAddress != null && selectedCoords != null){
            address_input.setText(selectedAddress);
            coords_input.setText(selectedCoords);
        }

        //generates coords via GeoCoder after address has been entered and user exits EditText field
        address_input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                /* When focus is lost check that the text field
                 * has valid values.
                 */
                if (!hasFocus) {
                    String address = address_input.getText().toString();
                    coords = GeoCoder.getLocationFromAddress(address);
                    coords_input.setText(coords);
                }
            }
        });

        //add text input to the SQLite db
        add_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String label = label_input.getText().toString();
                String address = address_input.getText().toString();
                String coords = coords_input.getText().toString();
                if (label.length() != 0 && address.length() != 0 && coords.length() != 0) {
                    AddData(label,address,coords);
                    label_input.setText("");
                    address_input.setText("");
                    coords_input.setText("");
                } else {
                    ToastMessage.message(getApplicationContext(),"You must put something in the text field!");
                }

            };
        });

        //goes to favorites page to see locations saved
        view_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LocationInput.this, Favorites.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.map:
                Intent map_intent = new Intent(LocationInput.this, OSMMap.class);
                startActivity(map_intent);
                break;
            case R.id.favorites:
                Intent favorites_intent = new Intent(LocationInput.this, Favorites.class);
                startActivity(favorites_intent);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }


    public void AddData(String label, String address, String coords) {
        boolean insertData = mDatabaseHelper.addData(label,address,coords);
        if (insertData) {
            ToastMessage.message(getApplicationContext(),"Data Successfully Inserted!");
        } else {
            ToastMessage.message(getApplicationContext(),"Something went wrong");
        }
    }
}