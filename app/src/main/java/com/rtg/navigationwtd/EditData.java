package com.rtg.navigationwtd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class EditData extends AppCompatActivity {

    EditText label_input,address_input,coords_input;
    ImageButton saveBt;
    DatabaseHelper mDatabaseHelper;
    private String selectedLabel;
    private String selectedAddress;
    private String selectedCoords;

    private int selectedID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_location);
        saveBt = findViewById(R.id.save_button);
        label_input = findViewById(R.id.label_input);
        address_input = findViewById(R.id.address_input);
        coords_input = findViewById(R.id.coords_input);
        mDatabaseHelper = new DatabaseHelper(this);

        //receive id and name of group
        Intent receivedIntent = getIntent();
        selectedID = receivedIntent.getIntExtra("id",-1);
        selectedLabel = receivedIntent.getStringExtra("name");
        selectedAddress = receivedIntent.getStringExtra("name");
        selectedCoords = receivedIntent.getStringExtra("name");
        label_input.setText(selectedLabel);
        address_input.setText(selectedAddress);
        coords_input.setText(selectedCoords);

        saveBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String label = label_input.getText().toString();
                String address = address_input.getText().toString();
                String coords = coords_input.getText().toString();
                if (label.length() != 0 && address.length() != 0 && coords.length() != 0) {

                } else {
                    ToastMessage.message(getApplicationContext(),"You must put something in the text field!");
                }

            }
        });
    }
}
