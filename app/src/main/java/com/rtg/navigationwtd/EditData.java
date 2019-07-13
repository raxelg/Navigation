package com.rtg.navigationwtd;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.rtg.MyApp;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EditData extends AppCompatActivity {

    EditText label_input,address_input,coords_input;
    FloatingActionButton saveBt, deleteBt;
    DatabaseHelper mDatabaseHelper;
    Geocoder geocoder = new Geocoder(MyApp.getContext(), Locale.getDefault());
    List<Address> addresses = new ArrayList();
    String coords;
    private String selectedLabel, selectedAddress, selectedCoords;
    private int groupID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_location);
        saveBt = findViewById(R.id.save_button);
        deleteBt = findViewById(R.id.delete_button);
        label_input = findViewById(R.id.label_input);
        address_input = findViewById(R.id.address_input);
        coords_input = findViewById(R.id.coords_input);
        mDatabaseHelper = new DatabaseHelper(this);
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);

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

        //receive id and name of group
        Intent receivedIntent = getIntent();
        groupID = receivedIntent.getIntExtra("groupID",-1);
        selectedLabel = receivedIntent.getStringExtra("label");
        selectedAddress = receivedIntent.getStringExtra("address");
        selectedCoords = receivedIntent.getStringExtra("coords");
        label_input.setText(selectedLabel);
        address_input.setText(selectedAddress);
        coords_input.setText(selectedCoords);

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String newLabel = label_input.getText().toString();
                String newAddress = address_input.getText().toString();
                String newCoords = coords_input.getText().toString();
                if (newLabel.length() != 0 && newLabel != selectedLabel) {
                    mDatabaseHelper.updateLabel(newLabel,groupID,selectedLabel);
                    if (newAddress.length() != 0 && newAddress != selectedAddress){
                        mDatabaseHelper.updateAddress(newAddress,groupID,selectedAddress);
                    }
                    if (newCoords.length() != 0 && newCoords != selectedCoords){
                        mDatabaseHelper.updateCoords(newCoords,groupID,selectedCoords);
                    }
                    Intent back = new Intent(EditData.this,Favorites.class);
                    startActivity(back);
                } else {
                    ToastMessage.message(getApplicationContext(),"You must put something in the text field(s)!");
                }

            }
        });

        deleteBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDatabaseHelper.deleteLocation(groupID,selectedLabel,selectedAddress,selectedCoords);
                Intent back = new Intent(EditData.this,Favorites.class);
                startActivity(back);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.edit_data_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId())
        {
            case R.id.map:
                Intent map_intent = new Intent(EditData.this, OSMMap.class);
                startActivity(map_intent);
                break;
            case R.id.favorites:
                Intent favorites_intent = new Intent(EditData.this, Favorites.class);
                startActivity(favorites_intent);
                break;
            default:
        }
        return super.onOptionsItemSelected(item);
    }
}
