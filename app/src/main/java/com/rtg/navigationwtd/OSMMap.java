package com.rtg.navigationwtd;

import android.Manifest;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.routing.GraphHopperRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.InfoWindow;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class OSMMap extends AppCompatActivity implements MapEventsReceiver, LocationListener, View.OnClickListener, SensorEventListener, LocationsMenuFragment.OnDataPass {

    private static final int REQUEST_LOCATION = 1;
    private int i;
    LocationManager locationManager;
    private String selectedCoords, dataInPrint, instruction, initialDirection, continueDirection, userOrientation, bearingDirection, btMessage;
    private double selectedLat, selectedLong, destinationLat, destinationLong, latitude, longitude, bearing, initialBearing;
    HashMap<String, String> directionBtInstructions = new HashMap<String, String>(); //each direction points to the appropriate BT char instruction

    ArrayList<Marker> markerpoints = new ArrayList<Marker>();
    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
    ArrayList<GeoPoint> nodes = new ArrayList<GeoPoint>();
    GeoPoint actualposPoint, nearestNode, destinationLoc;
    Marker actualposMarker, destinationPt, checkPoint;
    InfoWindow infoWindow;

    MapView map = null;
    MapEventsOverlay mapEventsOverlay;
    RotationGestureOverlay mRotationGestureOverlay;
    Road road;

    Button btRouting;
    FloatingActionButton dbBt, recenter, cancelBt;

    //compass intitialization
    ImageView compass_img;
    int mAzimuth;
    private SensorManager mSensorManager;
    private Sensor mRotationV, mAccelerometer, mMagnetometer;
    boolean haveSensor = false, haveSensor2 = false;
    float[] rMat = new float[9];
    float[] orientation = new float[3];
    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    FragmentManager fragmentManager;
    DatabaseHelper mDatabaseHelper;

    private BluetoothAdapter mBtAdapter;
//    private String address = "00:18:E4:40:00:06"; //PCB
    private String address = "20:16:08:22:50:32"; //PCB
    String IdBufferIn;
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.osm_map);
        Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        i = 0;
        fragmentManager = getSupportFragmentManager();
        mDatabaseHelper = new DatabaseHelper(this);
        directionBtInstructions.put("N", "a");
        directionBtInstructions.put("S", "b");
        directionBtInstructions.put("W", "c");
        directionBtInstructions.put("E", "d");
        directionBtInstructions.put("NW", "e");
        directionBtInstructions.put("NE", "f");
        directionBtInstructions.put("SW", "g");
        directionBtInstructions.put("SE", "h");

        map = findViewById(R.id.map);
        map.setTilesScaledToDpi(true);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapEventsOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(0, mapEventsOverlay);
        mRotationGestureOverlay = new RotationGestureOverlay(map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(this.mRotationGestureOverlay);

        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        GPSActivity();

        recenter = findViewById(R.id.recenter);
        recenter.setOnClickListener(this);

        btRouting = findViewById(R.id.bTRouting);
        btRouting.setOnClickListener(this);

        cancelBt = findViewById(R.id.cancel);

        dbBt = findViewById(R.id.database);

        actualposPoint = new GeoPoint(latitude, longitude);
        actualposMarker = new Marker(map);
        actualposMarker.setPosition(actualposPoint);
        map.getOverlays().add(1, actualposMarker);
        actualposMarker.setTitle("Ubicación Actual: \n" + latitude + ",\n " + longitude);
//        actualposMarker.setTitle("Ubicación Actual: " + GeoCoder.getAddressFromCoords(latitude,longitude));
        actualposMarker.setIcon(getResources().getDrawable(R.drawable.person));

        IMapController mapController = map.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(actualposPoint);
        actualposMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        if (savedInstanceState == null) {
            waypoints.add(actualposPoint);
        }
        markerpoints.add(actualposMarker);
        map.invalidate();

        //initialized to not have null object reference error when Object.equals() is called
        btMessage = "";
        instruction = "";

        //compass variables
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        compass_img = findViewById(R.id.img_compass);
//            txt_compass = findViewById(R.id.txt_azimuth);

        //compass start method
        start();

        //receiving coords from Favorites acitivity
        Intent receivedIntent = getIntent();
        selectedCoords = receivedIntent.getStringExtra("coords");

        if (selectedCoords != null) {
            String[] parsedCoords = selectedCoords.split(",");
            selectedLat = Double.parseDouble(parsedCoords[0]);
            selectedLong = Double.parseDouble(parsedCoords[1]);
            GeoPoint selectedPoint = new GeoPoint(selectedLat, selectedLong);

            //add destination marker from the received coords
            destinationPt = new Marker(map);
            destinationPt.setPosition(selectedPoint);
            map.getOverlays().add(destinationPt);
            destinationPt.setTitle("Final Destination");
            infoWindow = new MyInfoWindow(R.layout.destination_popup, map);
            destinationPt.setInfoWindow(infoWindow);
            destinationPt.setIcon(getResources().getDrawable(R.drawable.destination_marker));
            markerpoints.add(destinationPt);
            waypoints.add(selectedPoint);
            map.invalidate();
        } else {
            addFragment();
        }


        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        //IdBufferIn.setText("Dato: " + dataInPrint);
                        Toast.makeText(getBaseContext(), "InputStream: " + dataInPrint, Toast.LENGTH_SHORT).show();
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }
        };

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();

        dbBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OSMMap.this, Favorites.class);
                startActivity(intent);
            }
        });

        cancelBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                map.getOverlays().clear();
                map.getOverlays().add(0, mapEventsOverlay);
                map.getOverlays().add(1, actualposMarker);
                markerpoints.clear();
                markerpoints.add(actualposMarker);
                waypoints.clear();
                waypoints.add(actualposPoint);
                nodes.clear();
                map.invalidate();
                cancelBt.hide();
                addFragment();
                initialDirection = null;
                continueDirection = null;
                btMessage = "";
                instruction = "";
            }
        });
    }


    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));
        //::::::::: BLUETOOTH
        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);

        try {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La direcciÃ³n del Socket fallÃ³", Toast.LENGTH_LONG).show();
        }
        // Establece la conexiÃ³n con el Socket Bluettoth
        try {
            btSocket.connect();
        } catch (IOException e) {
            try {
                btSocket.close();
            } catch (IOException e2) {
            }
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();

        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
        mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        //:::::: BLUETOOTH
        try {
            // Cuando sale de la aplicación, esta parte no permite que se quedé abierto el socket
            btSocket.close();
        } catch (IOException e2) {
        }

        map.onPause(); //needed for compass, my location overlays, v6.0.0 and up
        stop();
    }

    public void GPSActivity() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps();

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            getLocation();
        }
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(OSMMap.this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (OSMMap.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(OSMMap.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 2, (LocationListener) this);

            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location location1 = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            Location location2 = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Log.d("Latitude", String.valueOf(latitude));
                Log.d("Longitude", String.valueOf(longitude));

            } else if (location1 != null) {
                latitude = location1.getLatitude();
                longitude = location1.getLongitude();

                Log.d("Latitude", String.valueOf(latitude));
                Log.d("Longitude", String.valueOf(longitude));

            } else if (location2 != null) {
                latitude = location2.getLatitude();
                longitude = location2.getLongitude();

                Log.d("Latitude", String.valueOf(latitude));
                Log.d("Longitude", String.valueOf(longitude));

            } else {

                Toast.makeText(this, "Unable to Trace your location", Toast.LENGTH_SHORT).show();

            }
        }
    }

    protected void buildAlertMessageNoGps() {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Please Turn ON your GPS Connection")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public boolean singleTapConfirmedHelper(GeoPoint p) {
        InfoWindow.closeAllInfoWindowsOn(map);
        return false;
    }

    @Override
    public boolean longPressHelper(final GeoPoint p) {
        if (!waypoints.isEmpty()) {
            int mpsize = markerpoints.size() - 1;
            if (!"Final Destination".equals(markerpoints.get(mpsize).getTitle())) {
                destinationPt = new Marker(map);
                destinationPt.setPosition(p);
                map.getOverlays().add(destinationPt);
                destinationPt.setTitle("Final Destination");
                infoWindow = new MyInfoWindow(R.layout.destination_popup, map);
                destinationPt.setInfoWindow(infoWindow);
                destinationPt.setIcon(getResources().getDrawable(R.drawable.destination_marker));
                markerpoints.add(destinationPt);
                waypoints.add(p);
                removeFramgent();
                map.invalidate();
                closeOptionsMenu();
            }
        } else {
            Toast.makeText(getApplicationContext(), "Debes primero marcar un origen", Toast.LENGTH_LONG).show();
        }

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bTRouting:
                if (waypoints.size() >= 2 && markerpoints.get(markerpoints.size() - 1).getTitle() == "Final Destination") {
                    map.getOverlays().clear();
                    map.getOverlays().addAll(markerpoints);
                    waypoints.set(0, actualposPoint);
                    Routing();
                    cancelBt.show();
                    map.getOverlays().add(0, mapEventsOverlay);
                } else {
                    Toast.makeText(getApplicationContext(), "Favor de seleccionar un destino", Toast.LENGTH_LONG).show();
                }
            case R.id.recenter:
                IMapController mapController = map.getController();
                mapController.setCenter(actualposPoint);
            default:
                break;
        }
    }

    public void Routing() {
        RoadManager roadManager = new GraphHopperRoadManager("53c0c417-5902-4c8c-882a-fe64e3fdb353", false);
//        RoadManager roadManager = new MapQuestRoadManager("PoVKuwnEeAujuzAHzqkRG9sIaG2iWA7E");
        roadManager.addRequestOption("unit=k");
//        roadManager.addRequestOption("routeType=pedestrian");
        roadManager.addRequestOption("vehicle=foot");
        roadManager.addRequestOption("optimize=true");
        road = roadManager.getRoad(waypoints);

        if (road.mStatus != Road.STATUS_OK) {
            Message.message(getApplicationContext(), "No hay ruta");
        } else {
            Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
            map.getOverlays().add(roadOverlay);
            map.invalidate();

            for (int i = 0; i < road.mNodes.size(); i++) {
                RoadNode node = road.mNodes.get(i);
                Marker nodeMarker = new Marker(map);
                nodeMarker.setPosition(node.mLocation);
                nodeMarker.setIcon(getResources().getDrawable(R.drawable.node_marker));
                nodeMarker.setTitle("Step " + i);
                nodeMarker.setSnippet(node.mInstructions);
                nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));

                GeoPoint g = new GeoPoint(road.mNodes.get(i).mLocation.getLatitude(), road.mNodes.get(i).mLocation.getLongitude());
                nodes.add(g);

                setInstructions(node, nodeMarker);
                map.getOverlays().add(nodeMarker);
            }
            map.invalidate();

            if (!nodes.isEmpty() && initialDirection == null) {
                if (actualposPoint.distanceToAsDouble(nodes.get(0)) > 8.5) {
                    initialBearing = actualposPoint.bearingTo(nodes.get(0));
                    userOrientation = Orientation.which_direction(mAzimuth);
                    bearingDirection = Orientation.which_direction((int) Math.round(initialBearing));
                    initialDirection = directionBtInstructions.get(Orientation.doubleOrientationDirection(userOrientation, bearingDirection));
                    MyConexionBT.write(initialDirection);
                    Message.message(getApplicationContext(), "Initial direction: " + initialDirection);
                } else if (actualposPoint.distanceToAsDouble(nodes.get(0)) <= 8.5 && continueDirection == null){
                    initialBearing = actualposPoint.bearingTo(nodes.get(1));
                    userOrientation = Orientation.which_direction(mAzimuth);
                    bearingDirection = Orientation.which_direction((int) Math.round(initialBearing));
                    continueDirection = directionBtInstructions.get(Orientation.doubleOrientationDirection(userOrientation, bearingDirection));
                    Message.message(getApplicationContext(), "Continue direction: " + continueDirection);
                    MyConexionBT.write(continueDirection);
                }
            }
        }
    }

    public void setInstructions(RoadNode node, Marker nMarker) {
        String[] instructions = node.mInstructions.split(" onto");
        if ("Turn slight left".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_slight_left);
            nMarker.setImage(icon);
        }
        if ("Turn slight right".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_slight_right);
            nMarker.setImage(icon);
        }
        if ("Turn sharp left".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_sharp_left);
            nMarker.setImage(icon);
        }
        if ("Turn sharp right".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_sharp_right);
            nMarker.setImage(icon);
        }
        if ("Turn left".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_turn_left);
            nMarker.setImage(icon);
        }
        if ("Turn right".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_turn_right);
            nMarker.setImage(icon);
        }
        if ("Make a U-turn".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_u_turn);
            nMarker.setImage(icon);
        }
        if ("Continue".equals(instructions[0])) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if ("Arrive at destination".equals(node.mInstructions)) {
            Drawable icon = getResources().getDrawable(R.drawable.ic_arrived);
            nMarker.setImage(icon);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        mapEventsOverlay = new MapEventsOverlay(this);
        map.getOverlays().add(0, mapEventsOverlay);

        actualposPoint.setLatitude(location.getLatitude());
        actualposPoint.setLongitude(location.getLongitude());
        actualposMarker.setPosition(actualposPoint);
        map.getOverlays().add(actualposMarker);
        actualposMarker.setTitle("Ubicación Actual: \n" + latitude + ",\n " + longitude);
//        actualposMarker.setTitle("Ubicación Actual: " + GeoCoder.getAddressFromCoords(latitude,longitude));
        map.invalidate();


        if (nodes.size() != 0) {
            nearestNode = Orientation.nearest_node(actualposPoint, nodes);
            bearing = actualposPoint.bearingTo(nodes.get(0));
        }

        if (!nodes.isEmpty()) {
            checkingDistance();
        }

        /** Rerouting to be implemented later**/
//        if(nodes.size() != 0 && actualposPoint.distanceToAsDouble(nearestNode) >= 30 && actualposPoint.distanceToAsDouble(nearestNode) < 50){
//            if(returnBtInstruction != directionBtInstructions.get(Orientation.doubleOrientationDirection(userOrientation,bearingDirection))) {
//                returnBtInstruction = directionBtInstructions.get(Orientation.doubleOrientationDirection(userOrientation,bearingDirection));
//                ToastMessage.message(getApplicationContext(), returnBtInstruction);
//                MyConexionBT.write(returnBtInstruction);
//            }
//        }
//        else if(nodes.size() != 0 && actualposPoint.distanceToAsDouble(nearestNode) >= 50){
//            Rerouting();
//        }

//            if(!nodes.isEmpty()) {
//                i++;
//                if(i%3 == 0) {
//                    checkPoint = new Marker(map);
//                    checkPoint.setPosition(actualposPoint);
//                    map.getOverlays().add(checkPoint);
//                    destinationPt.setTitle(String.valueOf(i-2));
//                    destinationPt.setIcon(getResources().getDrawable(R.drawable.checkpoint_flag));
//                    map.invalidate();
//                }
//            }
        }

    public void checkingDistance() {
        if (waypoints.size() >= 2) {
            if(road.mNodes != null && !road.mNodes.isEmpty()){
                for (int i = 0; i < road.mNodes.size(); i++) {
                    GeoPoint newGp = new GeoPoint(road.mNodes.get(i).mLocation.getLatitude(), road.mNodes.get(i).mLocation.getLongitude());
                    if (actualposPoint.distanceToAsDouble(newGp) <= 8.5){
                        String[] partstxtInst = road.mNodes.get(i).mInstructions.split(" onto");

                        if(instruction.equals(partstxtInst[0])) {
                            break;
                        } else {
                            instruction = partstxtInst[0];
                            btMessage = btInstructions.read_instructions(partstxtInst[0]);
                            Message.message(getApplicationContext(), "Normal direction: " + btMessage);
                            MyConexionBT.write(btMessage);
                        }
                    }
                }
            }
        }
    }

        public void Rerouting () {
            if (waypoints.size() >= 2 && markerpoints.get(markerpoints.size() - 1).getTitle() == "Final Destination") {
                map.getOverlays().clear();
                map.getOverlays().addAll(markerpoints);
                waypoints.set(0, actualposPoint);
                Routing();
                cancelBt.show();
                map.getOverlays().add(0, mapEventsOverlay);
            }
        }

        @Override
        public void onStatusChanged (String provider,int status, Bundle extras){
        }

        @Override
        public void onProviderEnabled (String provider){
        }

        @Override
        public void onProviderDisabled (String provider){
        }


        @Override
        public void onSensorChanged (SensorEvent event){
            if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
                SensorManager.getRotationMatrixFromVector(rMat, event.values);
                mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            }

            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
                mLastAccelerometerSet = true;
            } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
                mLastMagnetometerSet = true;
            }
            if (mLastAccelerometerSet && mLastMagnetometerSet) {
                SensorManager.getRotationMatrix(rMat, null, mLastAccelerometer, mLastMagnetometer);
                SensorManager.getOrientation(rMat, orientation);
                mAzimuth = (int) (Math.toDegrees(SensorManager.getOrientation(rMat, orientation)[0]) + 360) % 360;
            }

            mAzimuth = Math.round(mAzimuth);
            compass_img.setRotation(-mAzimuth);

            String where = "NW";

            where = Orientation.which_direction(mAzimuth);

//        txt_compass.setText(mAzimuth + "° " + where);
        }

        @Override
        public void onAccuracyChanged (Sensor sensor,int i){

        }

        //:::::::::: BLUETOOTH
        public void sendingWTDInstruction (String strgInstruction){
            MyConexionBT.write(strgInstruction);
        }

        public void Desconectar () {
            if (btSocket != null) {
                try {
                    btSocket.close();
                } catch (IOException e) {
                    Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
                }
            }
            finish();
        }

        private BluetoothSocket createBluetoothSocket (BluetoothDevice device) throws IOException {
            // Crea una conexión de salida segura para el dispositivo usando el servicio UUID
            return device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
        }

        // Comprueba que el dispositivo Bluetooth está disponible y solicita que se active si está deactivado
        private void VerificarEstadoBT () {
            // Comprueba que el dispositivo tiene Bluetooth y estÃ¡ encendido
            mBtAdapter = BluetoothAdapter.getDefaultAdapter();
            if (mBtAdapter == null) {
                Toast.makeText(getBaseContext(), "El Dispositivo no Soporta Bluetooth", Toast.LENGTH_SHORT).show();
            } else {
                if (!mBtAdapter.isEnabled()) {
                    // Solicita al Usuario que active el Bluetooth
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                }
            }
        }

        // Crea la clase que permite crear el evento de conexión
        private class ConnectedThread extends Thread {
            private final InputStream mmInStream;
            private final OutputStream mmOutStream;

            public ConnectedThread(BluetoothSocket socket) {
                InputStream tmpIn = null;
                OutputStream tmpOut = null;
                try {
                    tmpIn = socket.getInputStream();
                    tmpOut = socket.getOutputStream();
                } catch (IOException e) {
                }
                mmInStream = tmpIn;
                mmOutStream = tmpOut;
            }

            public void run() {
                byte[] buffer = new byte[1024];
                int bytes;

                // Se mantiene en modo escucha para determinar el ingreso de datos
                while (true) {
                    try {
                        bytes = mmInStream.read(buffer);
                        String readMessage = new String(buffer, 0, bytes);
                        // Envía los datos obtenidos hacia el evento via handler
                        bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    } catch (IOException e) {
                        break;
                    }
                }
            }

            // Envío de trama
            public void write(String input) {
                try {
                    mmOutStream.write(input.getBytes());
                } catch (IOException e) {
                    // Si no es posible enviar datos se cierra la conexión
//                Toast.makeText(getBaseContext(), "La conexión falló", Toast.LENGTH_LONG).show();
//                finish();
                }
            }

//        public void read(String input) {
//            try {
//                mmInStream.read(input.getBytes());
//                btInfo = mmInStream.toString();
//            } catch (IOException e) {
//                // Si no es posible enviar datos se cierra la conexión
//                Toast.makeText(getBaseContext(), "La conexión falló", Toast.LENGTH_LONG).show();
//                //finish();
//            }
//        }
        }

        private void start () {
            if (mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR) == null) {
                if ((mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) == null) || (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) == null)) {
                    noSensorsAlert();
                } else {
                    mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                    mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
                    haveSensor = mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
                    haveSensor2 = mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_UI);
                }
            } else {
                mRotationV = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
                haveSensor = mSensorManager.registerListener(this, mRotationV, SensorManager.SENSOR_DELAY_UI);
            }
        }

        private void noSensorsAlert () {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
            alertDialog.setMessage("Your device doesn't support the compass.")
                    .setCancelable(false)
                    .setNegativeButton("Close", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
//                            finish();
                        }
                    });
            alertDialog.show();
        }

        public void stop () {
            if (haveSensor && haveSensor2) {
                mSensorManager.unregisterListener(this, mAccelerometer);
                mSensorManager.unregisterListener(this, mMagnetometer);
            } else {
                if (haveSensor)
                    mSensorManager.unregisterListener(this, mRotationV);
            }
        }

        private class MyInfoWindow extends InfoWindow {
            public MyInfoWindow(int layoutResId, MapView mapView) {
                super(layoutResId, mapView);
            }

            public void onClose() {
            }

            public void onOpen(Object arg0) {
                destinationLoc = destinationPt.getPosition();
                destinationLat = destinationLoc.getLatitude();
                destinationLong = destinationLoc.getLongitude();
                LinearLayout popup_layout = mView.findViewById(R.id.popup);
                ImageButton addBt = mView.findViewById(R.id.add_location);
                TextView title = mView.findViewById(R.id.destination);
//                title.setText("Final Destination: \n" + GeoCoder.getAddressFromCoords(destinationLat,destinationLong));

                addBt.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String LatLong = destinationLat + "," + destinationLong;
                        String address = GeoCoder.getAddressFromCoords(destinationLat, destinationLong);

                        Intent addMarker = new Intent(OSMMap.this, LocationInput.class);
                        addMarker.putExtra("coords", LatLong);
                        addMarker.putExtra("address", address);
                        startActivity(addMarker);
                    }
                });
            }
        }

        @Override
        public void onDataPass (String data){
            if (data != null) {
                int mpsize = markerpoints.size() - 1;
                if ("Final Destination".equals(markerpoints.get(mpsize).getTitle())) {
                    Toast.makeText(getApplicationContext(), "Ya has seleccionado un destino", Toast.LENGTH_LONG).show();
                } else {
                    String[] parsedCoords = data.split(",");
                    selectedLat = Double.parseDouble(parsedCoords[0]);
                    selectedLong = Double.parseDouble(parsedCoords[1]);
                    GeoPoint selectedPoint = new GeoPoint(selectedLat, selectedLong);

                    destinationPt = new Marker(map);
                    destinationPt.setPosition(selectedPoint);
                    map.getOverlays().add(destinationPt);
                    destinationPt.setTitle("Final Destination");
                    infoWindow = new MyInfoWindow(R.layout.destination_popup, map);
                    destinationPt.setInfoWindow(infoWindow);
                    destinationPt.setIcon(getResources().getDrawable(R.drawable.destination_marker));
                    markerpoints.add(destinationPt);
                    waypoints.add(selectedPoint);
                    map.invalidate();
                    removeFramgent();
                }
            }
        }

        private void addFragment () {
            Cursor data = mDatabaseHelper.getData();
            if (data.getCount() != 0) {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                LocationsMenuFragment fragment = new LocationsMenuFragment();
                fragmentTransaction.add(R.id.locations_fragment, fragment);
                fragmentTransaction.commit();
            } else {
                Message.message(getApplicationContext(), "No hay ubicaciones guardadas en la base de datos para mostrar");
            }
        }

        private void removeFramgent () {
            Fragment fragment = fragmentManager.findFragmentById(R.id.locations_fragment);
            if (fragment != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commit();
            }
        }
    }