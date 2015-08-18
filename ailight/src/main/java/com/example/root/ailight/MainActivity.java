package com.example.root.ailight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    /**
     * Return Intent extra
     */
    public static String EXTRA_DEVICE_ADDRESS = "device_address";
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;

    /**
     * Newly discovered devices
     */
    private ArrayAdapter<String> mNewDevicesArrayAdapter;
    private TextView macAddr;
    protected  String curUuid;
    private SeekBar seakBar;

    protected int setakProgress;

    BluetoothSocket curClienSocket;

    private HashMap<String,BluetoothDevice> devices= new HashMap<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        macAddr = (TextView) findViewById(R.id.curMacAddr);
        Button scanButton = (Button) findViewById(R.id.button);
        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                doDiscovery();
//                v.setVisibility(View.GONE);
            }
        });
          seakBar = (SeekBar) findViewById(R.id.seekBar);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        ListView pairedListView = (ListView) findViewById(R.id.paired_devices);
        pairedListView.setAdapter(mNewDevicesArrayAdapter);
        pairedListView.setOnItemClickListener(mDeviceClickListener);
        if (!mBtAdapter.isEnabled()) {
            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            this.registerReceiver(mReceiver, filter);
            filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            this.registerReceiver(mReceiver, filter);
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);

        }else{
            initAdapor();
        }
        seakBar.setOnSeekBarChangeListener(seakBarListener);

    }


    private SeekBar.OnSeekBarChangeListener seakBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            setakProgress = progress;
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
                    sendCmd();
        }
    };

    protected void createOpenSock(final BluetoothDevice device){
            if(curClienSocket != null ){
                try {
                curClienSocket.close();
                }catch (Exception e){

                }
                curClienSocket = null;
            }

        new AsyncTask<Void, Void, Void>() {
              @Override
              protected Void doInBackground(Void... params) {

                  try {
                      Method m = device.getClass().getMethod("createRfcommSocket",
                              new Class[]{int.class});
                      BluetoothSocket temp = (BluetoothSocket) m.invoke(device, 1);
                      temp.connect();
                      curClienSocket = temp;
                  } catch (IOException e) {
                      e.printStackTrace();
                  } catch (InvocationTargetException e) {
                      e.printStackTrace();
                  } catch (NoSuchMethodException e) {
                      e.printStackTrace();
                  } catch (IllegalAccessException e) {
                      e.printStackTrace();
                  }
                  return null;

              }
          }.execute();

    }

    private void sendCmd() {
        if(curClienSocket == null){
            seakBar.setProgress(0);
            return;
        }
        final BluetoothSocket ttemp = curClienSocket;
        AsyncTask<Void,Void,Void> task = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                BluetoothSocket clienSocket= ttemp;
                try {
                 if(!clienSocket.isConnected()){
                     clienSocket.connect();
                 }
                    OutputStream outputStream = clienSocket.getOutputStream();
                    String cmd = createCmd();
                    outputStream.write(cmd.getBytes());
                    outputStream.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
        task.execute();

    }

    protected String createCmd(){
        String p = String.format("%04d",setakProgress);
         StringBuilder cmd = new StringBuilder("$Light1").append(p);
        String v  = cmd.substring("$Light".length(),"$Light10500".length());
        int vc =0;
        for(int i =0;i<v.length();i++){
            vc+= Integer.parseInt(v.substring(i,i+1));
        }
        cmd.append(String.format("%02d",vc)).append('#');
        return cmd.toString();
    }

    protected  void initAdapor(){

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

        // If there are paired devices, add each one to the ArrayAdapter
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                ParcelUuid[] uuids = device.getUuids();
                devices.put(uuids[0].getUuid().toString(),device);
                mNewDevicesArrayAdapter.add(uuids[0].getUuid().toString()+"\n"+device.getName() + "\n" + device.getAddress());
            }
        } else {
            mNewDevicesArrayAdapter.add("no devices have bean parired");
        }
    }


    /**
     * The on-click listener for all devices in the ListViews
     */
    private AdapterView.OnItemClickListener mDeviceClickListener
            = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            String info = ((TextView) v).getText().toString();
            String curadd = info.substring(info.length() - 17);
            curUuid = info.substring(0,"fa87c0d0-afac-11de-8a39-0800200c9a66".length());
            macAddr.setText(curadd);
            if(devices.containsKey(curUuid)){
                createOpenSock(devices.get(curUuid));
            }
        }
    };


    /**
     * The BroadcastReceiver that listens for discovered devices and changes the title when
     * discovery is finished
     */
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    ParcelUuid[] uuids = device.getUuids();
                    devices.put(uuids[0].getUuid().toString(),device);
                    mNewDevicesArrayAdapter.add(uuids[0].getUuid().toString()+"\n"+device.getName() + "\n" + device.getAddress());

                }
                // When discovery is finished, change the Activity title
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    mNewDevicesArrayAdapter.add("no device found");
                }
            }
        }
    };

    /**
     * Start device discover with the BluetoothAdapter
     */
    protected void doDiscovery() {
        Log.d("ailight", "doDiscovery()");

        // Indicate scanning in the title
        setProgressBarIndeterminateVisibility(true);
        setTitle("scan");

        // Turn on sub-title for new devices

        // If we're already discovering, stop it
        if (mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }

        // Request discover from BluetoothAdapter
        mBtAdapter.startDiscovery();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
//                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
//                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
//                    setupChat();
                } else {
                    // User did not enable Bluetooth or an error occurred
                    Log.d("ailight", "BT not enabled");
                    Toast.makeText(this,"bluetooth was not enabled",
                            Toast.LENGTH_SHORT).show();
//                    getActivity().finish();
                }
        }
    }


}
