package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.R.attr.value;
import static android.content.ContentValues.TAG;

/**
 * Created by Xingyu on 6/30/17.
 */

public class CtrlControlActivity extends Activity {
    private BluetoothGattCharacteristic characteristic;
    private String address;
    private UUID serviceId;
    private UUID charId;
    private TextView debugText;
    private Button startBtn;
    private Button stopBtn;
    private Timer timer = new Timer();
    private boolean started = false;
    private ArrayAdapter<String> adapter;
    private String Entry;
    private File file;
    //private Switch Led2SwitchToggle;
    //private static final String LED2OFF = "led2 0.0";
    //private static final String LED2ON = "0x01";
    //public BluetoothGatt mBluetoothGatt;
    //public static final UUID RX_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");


    private BluetoothLeService mBluetoothLeService;

    public boolean turnLEDon()
    {

        byte[] value = {(byte)1};
        characteristic.setValue(value);

        boolean status = mBluetoothLeService.mBluetoothGatt.writeCharacteristic(characteristic);

        return status;
    }
    public boolean turnLEDoff()
    {

        byte[] value = {(byte)0};
        characteristic.setValue(value);

        boolean status = mBluetoothLeService.mBluetoothGatt.writeCharacteristic(characteristic);

        return !status;
    }



    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            //mBluetoothLeService.connect(address);
            List<BluetoothGattService> list = mBluetoothLeService.getSupportedGattServices();
            for(BluetoothGattService s : list){
                if(s.getUuid().compareTo(serviceId) == 0){
                    characteristic = s.getCharacteristic(charId);
                    //mBluetoothLeService.disconnect();
                    return;
                }
            }
            debugText.setText("not find device");
            //mBluetoothLeService.disconnect();
            Log.e(TAG, "not find device");

        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            debugText.setText(action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action))
            {
                mBluetoothLeService.readCharacteristic(characteristic);
                turnLEDon();
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                String CTRL_Status = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                adapter.add("CTRL Status: " + CTRL_Status + "     " + new Date(System.currentTimeMillis()));
                Log.e(TAG, "adapter=: "+adapter);

                turnLEDoff();

                mBluetoothLeService.disconnect();

            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ctrl_control);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        serviceId = UUID.fromString(intent.getStringExtra("servic_id"));
        charId = UUID.fromString(intent.getStringExtra("character_id"));
        ((TextView)findViewById(R.id.address_txt)).setText("Address: "+address);
        ((TextView)findViewById(R.id.service_txt)).setText("Service UUID: " +serviceId.toString());
        ((TextView)findViewById(R.id.char_txt)).setText("Characteristic UUID: " + charId.toString());
        debugText = (TextView)findViewById(R.id.debug_txt);
        startBtn = (Button)findViewById(R.id.start_btn);
        stopBtn = (Button)findViewById(R.id.stop_btn);
        ListView listView = (ListView)findViewById(R.id.result_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);



        startBtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                mBluetoothLeService.connect(address);
                if(started) return;
                started = true;



                Toast.makeText (getBaseContext(), "Turn CTRL ON", Toast.LENGTH_SHORT).show();
                turnLEDon();
                Log.e(TAG, " On?: " + turnLEDon());

            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            //mBluetoothLeService.connect(address);
            @Override

            public void onClick(View v) {
                //mBluetoothLeService.connect(address);
                if(!started) return;
                started = false;
                turnLEDoff();
                Log.e(TAG, " Off?: " + turnLEDoff());
                Toast.makeText (getBaseContext(), "Turn CTRL OFF", Toast.LENGTH_SHORT).show();

            }
        });

        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, DeviceControlActivity.makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(address);
            Log.d(TAG, "Connect request result=" + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }
}
