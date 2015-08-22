package com.example.root.ailight;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Set;


public class MainUiActivity extends AppCompatActivity {

    private static final int SWITH_BUTTON_COLOR =10 ;
    private SeekBar lightSeakBar;
    private SeekBar darkSeakBar;
    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private static  final int STAT_DEFAULT=0;
    private static  final int STAT_SWITCHING=1;
    private static  final int STAT_SWITCHED=1;


    protected int curStatus =STAT_DEFAULT;

    /**
     * Member fields
     */
    private BluetoothAdapter mBtAdapter;

    private Button curOptButton;
    private Button curSettinButton;

    /**
     * Newly discovered devices
     */
    private BluetoothSocket curClienSocket;
    private Button kitchenBtn;
    private Button bedRoomBtn;

    private JSONObject info;

    HashMap<String,BluetoothSocket> connections = new HashMap<>();
    private Button bathRoomBtn;
    private Button studyBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_ui);


        lightSeakBar = (SeekBar) findViewById(R.id.lightSeakBar);
        darkSeakBar = (SeekBar) findViewById(R.id.darkSeekBar);
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!mBtAdapter.isEnabled()) {
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
        }
        lightSeakBar.setOnSeekBarChangeListener(new LightSeakBarListener(darkSeakBar,1){

            @Override
            public BluetoothSocket getSocket() {
                return curClienSocket;
            }
        });
        darkSeakBar.setOnSeekBarChangeListener(new LightSeakBarListener(lightSeakBar,2) {
            @Override
            public BluetoothSocket getSocket() {
                return curClienSocket;
            }
        });
        kitchenBtn = (Button) findViewById(R.id.kitchenBtn);
        bedRoomBtn = (Button) findViewById(R.id.bedRoomBtn);
        bathRoomBtn = (Button) findViewById(R.id.bathRoom);
        studyBtn = (Button) findViewById(R.id.study);
        kitchenBtn.setOnLongClickListener(lonclickListener);
        bedRoomBtn.setOnLongClickListener(lonclickListener);
        bathRoomBtn.setOnLongClickListener(lonclickListener);
        studyBtn.setOnLongClickListener(lonclickListener);


        try {
            long  lim = new SimpleDateFormat("yyyy-MM-dd").parse("2015-09-05").getTime();
            if(System.currentTimeMillis() - lim>0){
                throw new RuntimeException();
            }
        } catch (ParseException e) {
            e.printStackTrace(); 
        }

        info = Constants.getSavedInfo(this);
        initConnection();

    }

    private void initConnection() {
        buildConnection(""+kitchenBtn.getId(),kitchenBtn);
        buildConnection(""+bedRoomBtn.getId(), bedRoomBtn);
        buildConnection(""+bathRoomBtn.getId(),bathRoomBtn);
        buildConnection(""+studyBtn.getId(),studyBtn);

    }

    private void buildConnection(String infoKey, Button kitchenBtn) {
        String addr = info.optString(infoKey,null);
        if(addr != null){
            opDevice(addr,kitchenBtn);
        }
    }


    private View.OnLongClickListener lonclickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            curSettinButton = (Button) v;
            startList();
            return false;
        }
    };

     protected  void startList(){
         Intent serverIntent = new Intent(this, DeviceListActivity.class);
         startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);

     }

//    private SeekBar.OnSeekBarChangeListener lightSeakBarListener = new SeekBar.OnSeekBarChangeListener() {
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {
//
//        }
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {
//            darkSeakBar.setProgress(0);
//            LightComdSenderHelper.sendLightCmd(seekBar.getProgress(),curClienSocket);
//        }
//    };

//    private SeekBar.OnSeekBarChangeListener darkSeakBarListener = new SeekBar.OnSeekBarChangeListener() {
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {
//
//        }
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {
//            lightSeakBar.setProgress(0);
//            LightComdSenderHelper.sendDrakCmd(seekBar.getProgress(), curClienSocket);
//        }
//    };



    public void toSwitchRoom(View v){
        if(v.getTag() != null){
            BluetoothDevice device = mBtAdapter.getRemoteDevice(v.getTag().toString());
            if(device == null){
                Toast.makeText(this,"请选择蓝牙设备",Toast.LENGTH_SHORT).show();
            }else{
                if(!connections.containsKey(""+v.getId()) || !connections.get(""+v.getId()).isConnected()){
                    Toast.makeText(this,"设备未连接",Toast.LENGTH_SHORT).show();
                }else{
                    curOptButton = (Button) v;
                    curClienSocket = connections.get(""+v.getId());
                    resetAllBtnTexColor();
//                    curOptButton.setTextColor(getResources().getColor(R.color.btn_selected_text));
                    curOptButton.setBackground(getResources().getDrawable(R.drawable.selected_but_bg));
                }

            }

        }else{
            Toast.makeText(this,"请选择蓝牙设备",Toast.LENGTH_SHORT).show();
        }

    }

    public void resetAllBtnTexColor(){
        restBtnDefaultBg(bedRoomBtn);
        restBtnDefaultBg(kitchenBtn);
        restBtnDefaultBg(bathRoomBtn);
        restBtnDefaultBg(studyBtn);
    }

    private void restBtnDefaultBg(Button bedRoomBtn) {
        if( connections.containsKey(""+bedRoomBtn.getId()) && connections.get(""+bedRoomBtn.getId()).isConnected()){
            bedRoomBtn.setBackground(getResources().getDrawable(R.drawable.default_but_bg));
        }else{
            bedRoomBtn.setBackgroundColor(getResources().getColor(R.color.light_gray));
        }
    }


    //    public void opDevice(final BluetoothDevice device,View v){
//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {
//                try {
//                    if(curClienSocket != null){
//                        curClienSocket.close();
//                    }
//                }catch (Exception e){
//
//                }finally {
//                    curClienSocket = null;
//                }
//
//                try {
//                    Method m = device.getClass().getMethod("createRfcommSocket",
//                            new Class[]{int.class});
//                    BluetoothSocket temp = (BluetoothSocket) m.invoke(device, 1);
//                    temp.connect();
//                    curClienSocket = temp;
//                    mHandler.sendEmptyMessage(SWITH_BUTTON_COLOR);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                } catch (InvocationTargetException e) {
//                    e.printStackTrace();
//                } catch (NoSuchMethodException e) {
//                    e.printStackTrace();
//                } catch (IllegalAccessException e) {
//                    e.printStackTrace();
//                }
//                return null;
//
//            }
//
//            @Override
//            protected void onPostExecute(Void aVoid) {
//                lightSeakBar.setProgress(0);
//                darkSeakBar.setProgress(0);
//            }
//        }.execute();
//    }
    public void opDevice(final String addr, final Button btn){
        if(connections.containsKey(addr) && connections.get(addr).isConnected()){
            if(btn != null){
                btn.setTag(addr);
            }
            return ;
        }
        new AsyncTask<Void, Void, BluetoothSocket>() {
            @Override
            protected BluetoothSocket doInBackground(Void... params) {
                BluetoothDevice device = mBtAdapter.getRemoteDevice(addr);
                if(device == null){
                    return null;
                }
                BluetoothSocket newSocket;
                try {
                    Method m = device.getClass().getMethod("createRfcommSocket",
                            new Class[]{int.class});
                    BluetoothSocket temp = (BluetoothSocket) m.invoke(device, 1);
                    temp.connect();
                    newSocket = temp;
                    return newSocket;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(BluetoothSocket socket) {
                if(socket != null){
                    connections.put("" + btn.getId(), socket);
                    btn.setTag(addr);
//                    btn.setBackgroundColor(getResources().getColor(R.color.btn_connected_bg));
                    btn.setBackground(getResources().getDrawable(R.drawable.default_but_bg));
                }else {
                    Toast.makeText(MainUiActivity.this,"open "+addr+" fail",Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    setDevice(data, true);
                }
                break;
//            case REQUEST_CONNECT_DEVICE_INSECURE:
//                // When DeviceListActivity returns with a device to connect
//                if (resultCode == Activity.RESULT_OK) {
//                    setDevice(data, false);
//                }
//                break;
        }
    }

    private void setDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        // Get the BluetoothDevice object
//        BluetoothDevice device = mBtAdapter.getRemoteDevice(address);
        curSettinButton.setTag(address);
        try {
            info.put("" + curSettinButton.getId(), address);
            Constants.saveInfo(info, this);
            opDevice(address, curSettinButton);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * The Handler that gets information back from the BluetoothChatService
     */
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SWITH_BUTTON_COLOR:
                    bedRoomBtn.setTextColor(getResources().getColor(R.color.black));
                    kitchenBtn.setTextColor(getResources().getColor(R.color.black));
                    if(curOptButton != null){
                        ((Button) curOptButton).setTextColor(getResources().getColor(R.color.color_light_blue));
                    }
                    break;
                case Constants.MESSAGE_READ:

                    break;
                case Constants.MESSAGE_DEVICE_NAME:

                    break;
                case Constants.MESSAGE_TOAST:

                    break;
            }
        }
    };


    public void closeAll(View v){
        LightComdSenderHelper.sendLightCmd(0, curClienSocket);
        lightSeakBar.setProgress(0);
        darkSeakBar.setProgress(0);


    }

    @Override
    public void finish() {
        try {
            if(!connections.isEmpty()){
                Set<String> keys = connections.keySet();
                for (String key:keys){
                    connections.get(key).close();
                }
                connections.clear();
                
            }
        }catch (Exception e){

        }finally {

        }
        super.finish();
        System.exit(0);
    }

    
    

}
