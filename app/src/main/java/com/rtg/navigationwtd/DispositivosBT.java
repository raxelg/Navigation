package com.rtg.navigationwtd;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Set;

public class DispositivosBT extends AppCompatActivity {

    // Depuración de LOGCAT
    private static final String TAG = "DispositivosBT";
    // Declaración ListView
    ListView IdLista;
    // String que se enviará a la actividad principal, mainactivity
    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    // Declaración de Campos
    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dispositivos_bt);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        VerificarEstadoBT();

        // Inicializa el Arreglo que contendrá la lista de dispositivos Bluetooth vinculados
        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.nombre_dispositivos);
        // Presenta la lista de dispositivos vinculados en el ListView
        IdLista = (ListView) findViewById(R.id.IdLista);
        IdLista.setAdapter(mPairedDevicesArrayAdapter);
        IdLista.setOnItemClickListener(mDeviceClickListener);

        // Obtiene el adaptador local Bluetooth adapter
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        // Obtiene un conjunto de dispositivos actualmente emparejados y agrega a 'pairedDevices'
        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        // Adiciona un dispositivo previo emparejado al array
        if (pairedDevices.size() > 0)
        {
            for (BluetoothDevice device : pairedDevices)
            {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }

    }

    // Configura un (on-click) para la lista
    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener(){
        //public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
        public void onItemClick(AdapterView av, View v, int arg2, long arg3){
            // Obtener la Dirección MAC del dispositivo, que son los últimos 17 caracteres en la lista
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Realiza un Intent para iniciar la siguiente actividad
            // mientras toma un EXTRA_DEVICE_ADDRESS que es la dirección MAC
            Intent i = new Intent(DispositivosBT.this, OSMMap.class); // Parte a modificar
            i.putExtra(EXTRA_DEVICE_ADDRESS, address);
            startActivity(i);
        }
    };

    private void VerificarEstadoBT()
    {
        // Comprueba que el dispositivo tiene Bluetooth y está encendido
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBtAdapter == null)
        {
            Toast.makeText(getBaseContext(), "El Dispositivo no Soporta Bluetooth", Toast.LENGTH_SHORT).show();
        }
        else
        {
            if(mBtAdapter.isEnabled())
            {
                Log.d(TAG,"...Bluetooth Activado...");
            }
            else
            {
                // Solicita al Usuario que active el Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
            }
        }
    }
}
