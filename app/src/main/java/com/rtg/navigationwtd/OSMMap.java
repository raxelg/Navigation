package com.rtg.navigationwtd;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.bonuspack.overlays.GroundOverlay;
import org.osmdroid.bonuspack.routing.MapQuestRoadManager;
import org.osmdroid.bonuspack.routing.OSRMRoadManager;
import org.osmdroid.bonuspack.routing.Road;
import org.osmdroid.bonuspack.routing.RoadManager;
import org.osmdroid.bonuspack.routing.RoadNode;
import org.osmdroid.bonuspack.utils.PolylineEncoder;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.LineDrawer;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Polygon;
import org.osmdroid.views.overlay.Polyline;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.infowindow.BasicInfoWindow;
import org.osmdroid.views.overlay.infowindow.InfoWindow;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.text.DecimalFormat;
import java.util.ArrayList;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.UUID;

public class OSMMap extends AppCompatActivity implements MapEventsReceiver, LocationListener, View.OnClickListener {

    private static final int REQUEST_LOCATION = 1;
    LocationManager locationManager;
    double latitude, longitude;

    ArrayList<Marker> markerpoints = new ArrayList<Marker>();
    ArrayList<GeoPoint> waypoints = new ArrayList<GeoPoint>();
    GeoPoint actualposPoint;
    Marker actualposMarker;

    MapView map = null;
    MapEventsOverlay mapEventsOverlay;
    MyLocationNewOverlay mLocationOverlay;
    CompassOverlay mCompassOverlay;
    RotationGestureOverlay mRotationGestureOverlay;
    RoadManager roadManager = new OSRMRoadManager(this);
    Road road;

    Button btRouting;

    String IdBufferIn;
    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder DataStringIN = new StringBuilder();
    private ConnectedThread MyConexionBT;
    // Identificador único de servicio - SPP UUID
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // String para la direccion MAC
    // private static final UUID BTMODULEUUID = UUID.fromString("1234");
    // String para la dirección MAC
    private static String address = null;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        //important! set your user agent to prevent getting banned from the osm servers
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_osmmap);
        //Configuration.getInstance().setUserAgentValue(BuildConfig.APPLICATION_ID);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        map = (MapView) findViewById(R.id.map);
        map.setTileSource(TileSourceFactory.MAPNIK);
        mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);
        mRotationGestureOverlay = new RotationGestureOverlay(ctx, map);
        mRotationGestureOverlay.setEnabled(true);
        map.setMultiTouchControls(true);
        map.getOverlays().add(this.mRotationGestureOverlay);
        this.mCompassOverlay = new CompassOverlay(ctx, new InternalCompassOrientationProvider(ctx), map);
        this.mCompassOverlay.enableCompass();
        map.getOverlays().add(this.mCompassOverlay);


        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        GPSActivity();

        btRouting = (Button) findViewById(R.id.bTRouting);
        btRouting.setOnClickListener(this);

        actualposPoint = new GeoPoint(latitude, longitude);
        actualposMarker = new Marker(map);
        actualposMarker.setPosition(actualposPoint);
        map.getOverlays().add(1, actualposMarker);
        actualposMarker.setTitle("Ubicación Actual: "+latitude+", "+longitude);
        actualposMarker.setIcon(getResources().getDrawable(R.drawable.person));

        IMapController mapController = map.getController();
        mapController.setZoom(18.0);
        mapController.setCenter(actualposPoint);
        actualposMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        waypoints.add(actualposPoint);
        markerpoints.add(actualposMarker);


        bluetoothIn = new Handler() {
            public void handleMessage(android.os.Message msg) {
                if (msg.what == handlerState) {
                    String readMessage = (String) msg.obj;
                    DataStringIN.append(readMessage);

                    int endOfLineIndex = DataStringIN.indexOf("#");

                    if (endOfLineIndex > 0) {
                        String dataInPrint = DataStringIN.substring(0, endOfLineIndex);
                        //IdBufferIn.setText("Dato: " + dataInPrint);
                        DataStringIN.delete(0, DataStringIN.length());
                    }
                }
            }
        };
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        VerificarEstadoBT();
    }

    public void onResume(){
        super.onResume();
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this));

        //::::::::: BLUETOOTH
        // Consigue la dirección MAC desde DeviceListActivity via intent
        Intent intent = getIntent();
        // Consique la dirección MAC desde DeviceListActivity via EXTRA
        address = intent.getStringExtra(DispositivosBT.EXTRA_DEVICE_ADDRESS); // <<PARTE A MODIFICAR>>
        // Setea la dirección MAC
        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        try
        {
            btSocket = createBluetoothSocket(device);
        } catch (IOException e)
        {
            Toast.makeText(getBaseContext(), "La dirección del Socket falló", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el Socket Bluettoth
        try
        {
            btSocket.connect();
        } catch (IOException e)
        {
            try
            {
                btSocket.close();
            } catch (IOException e2){}
        }
        MyConexionBT = new ConnectedThread(btSocket);
        MyConexionBT.start();

        map.onResume(); //needed for compass, my location overlays, v6.0.0 and up
    }

    @Override
    public void onPause()
    {
        super.onPause();
        //:::::: BLUETOOTH
        try
        {
            // Cuando sale de la aplicación, esta parte no permite que se quedé abierto el socket
            btSocket.close();
        } catch (IOException e2){}

        map.onPause();  //needed for compass, my location overlays, v6.0.0 and up
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

            Location location2 = locationManager.getLastKnownLocation(LocationManager. PASSIVE_PROVIDER);

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();

                Log.d("Latitude", String.valueOf(latitude));
                Log.d("Longitude", String.valueOf(longitude));

            } else  if (location1 != null) {
                latitude = location1.getLatitude();
                longitude = location1.getLongitude();

                Log.d("Latitude", String.valueOf(latitude));
                Log.d("Longitude", String.valueOf(longitude));

            } else  if (location2 != null) {
                latitude = location2.getLatitude();
                longitude = location2.getLongitude();

                Log.d("Latitude", String.valueOf(latitude));
                Log.d("Longitude", String.valueOf(longitude));

            }else{

                Toast.makeText(this,"Unable to Trace your location",Toast.LENGTH_SHORT).show();

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

    @Override public boolean singleTapConfirmedHelper(GeoPoint p){
        InfoWindow.closeAllInfoWindowsOn(map);
        return false;
    }

    @Override public boolean longPressHelper(final GeoPoint p){
        PopupMenu popupMenu = new PopupMenu(OSMMap.this, btRouting);
        popupMenu.getMenuInflater().inflate(R.menu.popup_menu, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if("Marcar Destino".equals(item.getTitle()))
                {
                    if(!waypoints.isEmpty())
                    {
                        int mpsize = markerpoints.size()-1;
                        if("END POINT".equals(markerpoints.get(mpsize).getTitle()))
                        {
                            Toast.makeText(getApplicationContext(), "Ya has seleccionado un destino", Toast.LENGTH_LONG).show();;
                        }
                        else
                        {
                            Marker m = new Marker(map);
                            m.setPosition(p);
                            map.getOverlays().add(m);
                            m.setTitle("END POINT");
                            m.setIcon(getResources().getDrawable(R.drawable.marker_destination));
                            markerpoints.add(m);
                            waypoints.add(p);
                            map.invalidate();
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Debes primero marcar un origen", Toast.LENGTH_LONG).show();
                    }
                }
                if("Agregar Escala".equals(item.getTitle()))
                {
                    if(!waypoints.isEmpty())
                    {
                        int mpsize = markerpoints.size()-1;
                        if("END POINT".equals(markerpoints.get(mpsize).getTitle()))
                        {
                            Marker bm;
                            bm = markerpoints.get(mpsize);

                            double lat = waypoints.get(mpsize).getLatitude();
                            double lng = waypoints.get(mpsize).getLongitude();
                            GeoPoint bp = new GeoPoint(lat, lng);

                            Marker m = new Marker(map);
                            m.setPosition(p);
                            map.getOverlays().add(m);
                            m.setTitle("VIA POINT");
                            m.setIcon(getResources().getDrawable(R.drawable.marker_via));

                            markerpoints.add(bm);
                            markerpoints.get(mpsize+1).setTitle("END POINT");
                            markerpoints.get(mpsize+1).setIcon(getResources().getDrawable(R.drawable.marker_destination));

                            markerpoints.set(mpsize, m);

                            waypoints.add(bp);
                            waypoints.set(mpsize, p);

                            map.invalidate();
                        }
                        else
                        {
                            Marker m = new Marker(map);
                            m.setPosition(p);
                            map.getOverlays().add(m);
                            m.setTitle("VIA POINT");
                            m.setIcon(getResources().getDrawable(R.drawable.marker_via));
                            markerpoints.add(m);
                            waypoints.add(p);

                            map.invalidate();
                        }
                    }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Debes primero marcar un origen", Toast.LENGTH_LONG).show();
                    }
                }
                return false;
            }
        });
        popupMenu.show();

        return false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.bTRouting:
                if(waypoints.size()>=2)
                {
                    map.getOverlays().clear();
                    map.getOverlays().addAll(markerpoints);
                    waypoints.set(0, actualposPoint);
                    Routing();
                    map.getOverlays().add(0, mapEventsOverlay);
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Favor de seleccionar un origen o escalas", Toast.LENGTH_LONG).show();
                }

            default:
                break;
        }
    }

    public void Routing()
    {
        RoadManager roadManager = new MapQuestRoadManager("exUd1wMxLoc3RMoIO3G6eOrDAHx5hQ90");
        roadManager.addRequestOption("unit=k");
        roadManager.addRequestOption("routeType=pedestrian");
        road = roadManager.getRoad(waypoints);
        Polyline roadOverlay = RoadManager.buildRoadOverlay(road);
        map.getOverlays().add(roadOverlay);

        for (int i=0; i<road.mNodes.size(); i++){
            RoadNode node = road.mNodes.get(i);
            Marker nodeMarker = new Marker(map);
            nodeMarker.setPosition(node.mLocation);
            nodeMarker.setIcon(getResources().getDrawable(R.drawable.marker_node));
            nodeMarker.setTitle("Step "+i);
            nodeMarker.setSnippet(node.mInstructions);
            nodeMarker.setSubDescription(Road.getLengthDurationText(this, node.mLength, node.mDuration));

            setInstructions(node, nodeMarker);
            map.getOverlays().add(nodeMarker);
        }

        GeoPoint node0Gp = new GeoPoint(road.mNodes.get(0).mLocation.getLatitude(), road.mNodes.get(0).mLocation.getLongitude());
        ArrayList<GeoPoint> newGps = new ArrayList<>();
        newGps.add(actualposPoint);
        newGps.add(node0Gp);
        Polyline stepZero = new Polyline();
        stepZero.setColor(Color.RED);
        stepZero.setWidth(4);
        stepZero.setPoints(newGps);
        map.getOverlayManager().add(stepZero);

        map.invalidate();
    }

    public void setInstructions(RoadNode node, Marker nMarker)
    {
        String[] partstxtInst = node.mInstructions.split(" on");
        if("Make a slight left".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_slight_left);
            nMarker.setImage(icon);
        }
        if("Make a slight right".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_slight_right);
            nMarker.setImage(icon);
        }
        if("Make a sharp left".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_sharp_left);
            nMarker.setImage(icon);
        }
        if("Make a sharp right".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_sharp_right);
            nMarker.setImage(icon);
        }
        if("Turn left".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_turn_left);
            nMarker.setImage(icon);
        }
        if("Turn right".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_turn_right);
            nMarker.setImage(icon);
        }
        if("U turn".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_u_turn);
            nMarker.setImage(icon);
        }
        if("Go north".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("Go northwest".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("Go northeast".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("Go south".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("Go southeast".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("Go southwest".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("Go west".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("Go east".equals(partstxtInst[0]))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_continue);
            nMarker.setImage(icon);
        }
        if("You have arrived at your destination".equals(node.mInstructions))
        {
            Drawable icon = getResources().getDrawable(R.drawable.ic_arrived);
            nMarker.setImage(icon);
        }
    }

    public void getBTInstructions(String strInstructions)
    {
        if("Make a slight left".equals(strInstructions))
        {
            sendingWTDInstruction("e");
        }
        if("Make a slight right".equals(strInstructions))
        {
            sendingWTDInstruction("f");
        }
        if("Make a sharp left".equals(strInstructions))
        {
            sendingWTDInstruction("g");
        }
        if("Make a sharp right".equals(strInstructions))
        {
            sendingWTDInstruction("h");
        }
        if("Turn left".equals(strInstructions))
        {
            sendingWTDInstruction("c");
        }
        if("Turn right".equals(strInstructions))
        {
            sendingWTDInstruction("d");
        }
        if("U turn".equals(strInstructions))
        {
            sendingWTDInstruction("i");
        }
        if("Go north".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("Go northwest".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("Go northeast".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("Go south".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("Go southeast".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("Go southwest".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("Go west".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("Go east".equals(strInstructions))
        {
            sendingWTDInstruction("a");
        }
        if("You have arrived at your destination".equals(strInstructions))
        {
            sendingWTDInstruction("i");
        }
    }

    @Override
    public void onLocationChanged(Location location) {

        mapEventsOverlay = new MapEventsOverlay(this, this);
        map.getOverlays().add(0, mapEventsOverlay);

        actualposPoint.setLatitude(location.getLatitude());
        actualposPoint.setLongitude(location.getLongitude());
        actualposMarker.setPosition(actualposPoint);
        map.getOverlays().set(1, actualposMarker);
        actualposMarker.setTitle("Ubicación Actual: "+latitude+", "+longitude);
        map.invalidate();

        checkingDistance();
    }

    public void checkingDistance(){
        if (waypoints.size() >= 2) {
            if (!road.mNodes.isEmpty())
            {
                for (int i=0; i<road.mNodes.size(); i++) {
                    GeoPoint newGp = new GeoPoint(road.mNodes.get(i).mLocation.getLatitude(), road.mNodes.get(i).mLocation.getLongitude());

                    //if (actualposPoint.distanceTo(newGp) <= 8) {
                    if (actualposPoint.distanceToAsDouble(newGp) <= 15) {
                        String[] partstxtInst = road.mNodes.get(i).mInstructions.split(" on");
                        Toast.makeText(getApplicationContext(), partstxtInst[0], Toast.LENGTH_LONG).show();

                        if("You have arrived at your destination".equals(road.mNodes.get(i).mInstructions))
                        {
                            getBTInstructions(road.mNodes.get(i).mInstructions);
                        } else {
                            getBTInstructions(partstxtInst[0]);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
    @Override
    public void onProviderEnabled(String provider) {
    }
    @Override
    public void onProviderDisabled(String provider) {
    }

    //:::::::::: BLUETOOTH
    public void sendingWTDInstruction(String strgInstruction) {
        MyConexionBT.write(strgInstruction);
    }

    public  void Desconectar() {
        if (btSocket != null) {
            try {
                btSocket.close();
            } catch (IOException e) {
                Toast.makeText(getBaseContext(), "Error", Toast.LENGTH_SHORT).show();
            }
        }
        finish();
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException
    {
        // Crea una conexión de salida segura para el dispositivo usando el servicio UUID
        return device.createInsecureRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    // Comprueba que el dispositivo Bluetooth está disponible y solicita que se active si está deactivado
    private void VerificarEstadoBT()
    {
        if(btAdapter == null)
        {
            Toast.makeText(getBaseContext(), "El dispositivo no soporta Bluetooth", Toast.LENGTH_LONG).show();
        }
        else
        {
            if(btAdapter.isEnabled())
            {
            }
            else
            {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }

    // Crea la clase que permite crear el evento de conexión
    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true)
            {
                try
                {
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envía los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1,readMessage).sendToTarget();
                } catch (IOException e)
                {
                    break;
                }
            }
        }

        // Envío de trama
        public void write(String input)
        {
            try
            {
                mmOutStream.write(input.getBytes());
            } catch (IOException e) {
                // Si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La conexión falló", Toast.LENGTH_LONG).show();
                //finish();
            }
        }
    }
}
