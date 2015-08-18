package com.example.root.ailight;

import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by root on 15-8-16.
 */
public class LightComdSenderHelper {


    public static void sendLightCmd(int progress,BluetoothSocket socket){
        sendCmd(progress,socket,1);
    }
    public static void sendDrakCmd(int progress,BluetoothSocket socket){
        sendCmd(progress,socket,2);
    }


    protected static void sendCmd(final int progress,BluetoothSocket curClienSocket , final int type){
        if(curClienSocket == null){
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
                    String cmd = createCmd(progress,type);
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
    protected static String createCmd(int progress,int type){
        String p = String.format("%04d",progress);
        StringBuilder cmd = new StringBuilder("$Light").append(type).append(p);
        String v  = cmd.substring("$Light".length(),"$Light10500".length());
        int vc =0;
        for(int i =0;i<v.length();i++){
            vc+= Integer.parseInt(v.substring(i,i+1));
        }
        cmd.append(String.format("%02d",vc)).append('#');
        return cmd.toString();
    }

}
