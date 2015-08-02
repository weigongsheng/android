package com.hiuzhong.yuxun.receiver;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class MesgRciever extends BroadcastReceiver {

    NotificationManager mn=null;
    Notification notification=null;
    Context ct=null;
    MesgRciever receiver;
    
    public MesgRciever(Context c){
        ct=c;
        receiver=this;
    }

    //注册
    public  void registerAction(String action){
        IntentFilter filter=new IntentFilter();
        filter.addAction(action);
        ct.registerReceiver(receiver, filter);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
        String msg=intent.getStringExtra("msg");
        int id=intent.getIntExtra("who", 0);
        if(intent.getAction().equals("com.cbin.sendMsg")){

        }
    }
}