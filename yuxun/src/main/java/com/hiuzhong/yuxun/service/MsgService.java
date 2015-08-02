package com.hiuzhong.yuxun.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;

import com.hiuzhong.yuxun.ChatActivity;
import com.hiuzhong.yuxun.R;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.dao.MsgCountDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.receiver.OnReceiveCountMsgListener;
import com.hiuzhong.yuxun.receiver.OnReceiveMsgListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MsgService extends Service {

    public static MsgService  msgService = null;
    //获取消息线程
    private MessageThread messageThread = null;
    //点击查看
    private Intent messageIntent = null;
//    private PendingIntent messagePendingIntent = null;

    //通知栏消息
    private int messageNotificationID = 1000;
    private Notification messageNotification = null;
    private NotificationManager messageNotificatioManager = null;
    protected MessageDbManager msgDao;
    protected ContactsDbManager contDao;

    protected OnReceiveCountMsgListener countMsgListener;

    protected  OnReceiveMsgListener curContatListener;

    protected MsgCountDbManager countDbo;

    public void setCountMsgListener(OnReceiveCountMsgListener listener){
        this.countMsgListener = listener;
    }

    public void setCurContatListener(OnReceiveMsgListener listener){
        this.curContatListener = listener;
    }

    public static final MsgService  getCurService(){
        return msgService;
    }

//    private JSONObject myAccount;
    String account;
    String pwd;

    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        countDbo = new MsgCountDbManager(this);
        msgService = this;
        //初始化
        messageNotification = new Notification();
        messageNotification.tickerText = "新消息";
        messageNotification.defaults = Notification.DEFAULT_SOUND;
        messageNotificatioManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        messageIntent = new Intent(this, ChatActivity.class);
        messageIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//        messagePendingIntent = PendingIntent.getActivity(this, 0, messageIntent, 0);
        msgDao = new MessageDbManager(this);
        contDao = new ContactsDbManager(this);
        //开启线程
        messageThread = new MessageThread();
        messageThread.isRunning = true;
        messageThread.start();
        JSONObject json = ActivityHelper.getUserInfo(this);
        account =json.optString("account");
        pwd =json.optString("pwd");
        json= null;
        return super.onStartCommand(intent, flags, startId);
    }

    /**
     * 从服务器端获取消息
     */
    class MessageThread extends Thread {
        //运行状态，下一步骤有大用
        public boolean isRunning = true;
        public void run() {
            while (true) {
                try {
                    //休息3秒
                    Thread.sleep(3 * 1000);
                    //获取服务器消息
                    JSONObject msg = getServerMessage();
                    if (msg == null || msg.getJSONObject("data") == null) {
                        continue;
                    }
                    JSONArray msges = msg.getJSONObject("data").getJSONArray("AppToApp");
                    if (msg != null && msges.length() > 0) {
                        sendMsgCount(msges);
                        String account = msges.getJSONObject(msges.length() - 1).optString("SendAppNumber");
                        Notification.Builder builder = new Notification.Builder(MsgService.this);
                        builder.setAutoCancel(true);
                        builder.setSmallIcon(R.mipmap.ic_launcher);
                        builder.setContentTitle("新消息" );
                        builder.setContentText(account + ":"
                                + msges.getJSONObject(msges.length() - 1).optString("MsgContent"));
                        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE);
                        messageIntent.putExtra(ChatActivity.INTENT_PARA_CONTACT, account);
                        builder.setContentIntent(PendingIntent.getActivity(MsgService.this, 0, messageIntent, 0));
                        Bundle b = new Bundle();
                        b.putString(ChatActivity.INTENT_PARA_CONTACT, account);

//                    builder.addExtras(b);
//                        //更新通知栏
//                        messageNotification.setLatestEventInfo(MsgService.this,
//                                "新消息", "奥巴马宣布,本拉登兄弟挂了!" + serverMessage, messagePendingIntent);
                        messageNotificatioManager.notify(messageNotificationID, builder.build());
                        //每次通知完，通知ID递增一下，避免消息覆盖掉
                        saveMsg(msg);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                } catch (JSONException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }


    protected  void sendMsgCount(JSONArray msg) throws JSONException {
        HashMap<String,Integer> allCount = new HashMap<>();
        for(int i=0,j=msg.length();i<j;i++){
            JSONObject m = msg.getJSONObject(i);
            String account = m.optString("SendAppNumber");
            Integer c = allCount.get(account);
            if(c == null){
                c=0;
            }
            ++c;
            allCount.put(account,c);
            if(curContatListener != null){
                curContatListener.onMessage(account,m.optString("MsgContent"),m.optString("InsertTime"));
            }
        }
        Set<Map.Entry<String, Integer>> data = allCount.entrySet();
        for(Map.Entry<String, Integer> en:data){
            countDbo.add(en.getKey(),en.getValue());
        }
        if(countMsgListener != null){
          countMsgListener.onMessage(allCount);
        }
    }

    /**
     * 这里以此方法为服务器Demo，仅作示例
     *
     * @return 返回服务器要推送的消息，否则如果为空的话，不推送
     */
    public JSONObject getServerMessage() {
        // WebServiceHelper.queryMsg(account,pwd)
        return WebServiceHelper.queryMsg(account,pwd);
//        String msg = "{\"code\":0,\"data\":{\"AppToApp\":[{\"SendAppNumber\":66621979360,\"MsgContent\":\"你们是因为太\",\"InsertTime\":\"2015-08-01 09:49:11\"},{\"SendAppNumber\":66621979360,\"MsgContent\":\"我是海浪声我\",\"InsertTime\":\"2015-08-01 09:49:16\"}]},tip:\"取得App到App的数据成功\"}";
//        try {
//            return new JSONObject(msg);
//        } catch (JSONException e) {
//           e.printStackTrace();
//        }
//        return null;
    }

    protected void saveMsg(JSONObject source) throws JSONException {
        JSONArray msgs = source.getJSONObject("data").getJSONArray("AppToApp");
        for (int i = 0; i <msgs.length() ; i++) {
            JSONObject msg = msgs.getJSONObject(i);
            msgDao.addNewMsg(msg.optString("SendAppNumber"),MessageDbManager.MSG_TYPE_RECEIVE,msg.optString("MsgContent"),msg.optString("InsertTime"));
        }
    }

    @Override
    public void onDestroy() {
        messageThread.interrupt();
        messageNotificatioManager.cancel(messageNotificationID);
        messageNotificatioManager.cancelAll();
        msgDao.closeDB();
        contDao.closeDB();
        countDbo.closeDB();
        super.onDestroy();
    }
}
