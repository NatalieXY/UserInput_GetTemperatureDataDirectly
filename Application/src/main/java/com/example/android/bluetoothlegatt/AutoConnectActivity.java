package com.example.android.bluetoothlegatt;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import static android.content.ContentValues.TAG;

/**
 * Created by Xingyu on 6/27/17.
 */

public class AutoConnectActivity extends Activity {
    private BluetoothGattCharacteristic characteristic;
    private BluetoothGattCharacteristic characteristicCTRL2;
    private BluetoothAdapter mBluetoothAdapter;
    public BluetoothGatt mBluetoothGatt;
    private String address;
    private UUID serviceId;
    private UUID charId;
    private UUID LEDserviceId;
    private UUID LEDcharId;

    private TextView debugText;
    private Button startBtn;
    private Button stopBtn;
    private Button LEDstartBtn;
    private Button LEDstopBtn;

    private Timer timer = new Timer();
    //private boolean started = false;
    private ArrayAdapter<String> adapter;
    private String Entry;
    private File file;
    private boolean check=false;

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


    private TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {

            mBluetoothLeService.connect(address);
        }
    };


    // Code to manage Service lifecycle.

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override

        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(address);


                List<BluetoothGattService> list = mBluetoothLeService.getSupportedGattServices();
                for(BluetoothGattService s : list) {
                    if (check==true){
                        if (s.getUuid().compareTo(LEDserviceId) == 0) {

                            characteristic = s.getCharacteristic(LEDcharId);
                            Log.e(TAG, " characteristic temp?: " + s.getCharacteristic(LEDcharId));
                            mBluetoothLeService.disconnect();


                            return;
                        }
                    }
                    if (s.getUuid().compareTo(serviceId) == 0) {

                        characteristic = s.getCharacteristic(charId);
                        Log.e(TAG, " characteristic temp?: " + s.getCharacteristic(charId));
                        mBluetoothLeService.disconnect();


                        return;
                    }

                }


            mBluetoothLeService.disconnect();
            Log.e(TAG, "not find device");
            debugText.setText("not find device");
            if(debugText.toString().equals("not find device")){
                mBluetoothLeService.connect(address);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            debugText.setText(action);
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {

                    mBluetoothLeService.readCharacteristic(characteristic);
                    if(check==true){
                        turnLEDon();
                    }

            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action))

            {


                    String temperature = intent.getStringExtra(BluetoothLeService.EXTRA_DATA);
                    adapter.add("Temperature: " + temperature + "°C     " + new Date(System.currentTimeMillis()));

                    Entry = address + "," + new Date(System.currentTimeMillis()).toString() + "," + temperature.toString() + "\n";

                    try {
                        FileOutputStream out = new FileOutputStream(file, true);
                        out.write(Entry.getBytes());
                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    mBluetoothLeService.disconnect();
                }

        }
    };

    @Override
    protected void onCreate(@org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.autoconnect);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent1 = getIntent();
        address = intent1.getStringExtra("address");
        //Log.e(TAG, " Address: " + address);
        serviceId = UUID.fromString(intent1.getStringExtra("service_id"));
        //Log.e(TAG, " serviceId: " + serviceId);
        charId = UUID.fromString(intent1.getStringExtra("character_id"));
        //Log.e(TAG, " charId: " + charId);
        LEDserviceId = UUID.fromString(intent1.getStringExtra("ctrl2_service_id"));
        LEDcharId = UUID.fromString(intent1.getStringExtra("ctrl2_character_id"));

        ((TextView)findViewById(R.id.address_txt)).setText(address);
        ((TextView)findViewById(R.id.service_txt)).setText(serviceId.toString());
        ((TextView)findViewById(R.id.char_txt)).setText(charId.toString());
        debugText = (TextView)findViewById(R.id.debug_txt);
        startBtn = (Button)findViewById(R.id.start_btn);
        stopBtn = (Button)findViewById(R.id.stop_btn);
        LEDstartBtn = (Button)findViewById(R.id.ctrl_start_btn);
        LEDstopBtn = (Button)findViewById(R.id.ctrl_stop_btn);


        ListView listView = (ListView)findViewById(R.id.result_list);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        listView.setAdapter(adapter);




        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //External Storage
                String state;
                state = Environment.getExternalStorageState();
                if (Environment.MEDIA_MOUNTED.equals(state)) {
                    File root = Environment.getExternalStorageDirectory();
                    File Dir = new File(root.getAbsolutePath() + "/DataFile");
                    if (!Dir.exists()) {
                        Dir.mkdir();
                    }
                    file = new File(Dir, "Temperature.csv");

                }
                mBluetoothLeService.connect(address);

                    timer.schedule(timerTask, 0, 1000 * 5); }//1000 => 1s   *600  => 10 min  3600=>1h
                });


        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {




                timer.cancel();
                mBluetoothLeService.disconnect();
            }
        });

        LEDstartBtn.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View v)
            {
                mBluetoothLeService.connect(address);
                check=true;



                Toast.makeText (getBaseContext(), "Turn CTRL ON", Toast.LENGTH_SHORT).show();
                turnLEDon();
                Log.e(TAG, " On?: " + turnLEDon());

            }
        });

        LEDstopBtn.setOnClickListener(new View.OnClickListener() {
            //mBluetoothLeService.connect(address);
            @Override

            public void onClick(View v) {


                turnLEDoff();
                Log.e(TAG, " Off?: " + turnLEDoff());
                Toast.makeText (getBaseContext(), "Turn CTRL OFF", Toast.LENGTH_SHORT).show();

            }
        });



        Intent gattServiceIntent = new Intent(AutoConnectActivity.this, BluetoothLeService.class);
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
