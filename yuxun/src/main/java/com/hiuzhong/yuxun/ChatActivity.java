package com.hiuzhong.yuxun;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.vo.Contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import android.os.Handler;

import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.util.logging.LogRecord;


public class ChatActivity extends Activity {

    protected TextView msgInput;
    private LinearLayout msgContainer;
    private ScrollView msgScrollView;
    private MessageDbManager msgDao;
    private Bitmap contactFaceBitmap;

    protected Handler handler;
    protected String lasMsg;
    private Contact cant;
    private String SERVICE_URL;
    private String SERVICE_NAMESPACE;
    private String methodName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        msgInput = (TextView) findViewById(R.id.msgEdit);
        msgContainer = (LinearLayout) findViewById(R.id.msgScrollContainer);
        msgScrollView = (ScrollView) findViewById(R.id.msgScrollView);
        msgDao = new MessageDbManager(this);
        initData();
        showLostMsg();
    }

    private void showLostMsg(){
        List<String[]> msgs = msgDao.query(3, 0,String.valueOf(cant.id));
        if(msgs == null){
            return ;
        }
        for (int i = msgs.size()-1; i >=0; i--) {
            String[] msg = msgs.get(i);
            if(msg!= null){
                if(MessageDbManager.MSG_TYPE_SEND.equals(msg[3])){
                    inflateSendMst(msg[1]);
                }else{
                    inflateReceivedMsg(msg[1]);
                }
            }
        }
    }



    private void initData() {
//       Map<String,String> source= (Map<String, String>) getIntent().getSerializableExtra("contactInfo");
//        contactId = source.get("contactId");
        ContactsDbManager dao = new ContactsDbManager(this);
        cant =dao.queryById(getIntent().getStringExtra("contactId"));

        contactFaceBitmap = ImageLoaderUtil.loadFromFile(this, cant.faceImgPath);
        msgInput.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                handler.sendEmptyMessage(0);
            }
        });
        handler = new Handler() {
            public void handleMessage(Message msg) {
                msgScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        };
    }



    public void sendMsg(View textView) {
        CharSequence msg = msgInput.getText();
        if (msg != null && ! msg.equals("")) {
            lastMsgChange( msg.toString());
            inflateSendMst(msg);
            msgDao.add(String.valueOf(cant.id), MessageDbManager.MSG_TYPE_SEND, msg.toString());
            showReceivedMsg("yes " + msg);
            handler.sendEmptyMessage(0);

        }
    }
    public void showReceivedMsg(CharSequence msg) {
        if (msg != null) {
            lastMsgChange(msg.toString());
            inflateReceivedMsg(msg);
            msgDao.add(String.valueOf(cant.id), MessageDbManager.MSG_TYPE_RECEIVE, msg.toString());
            handler.sendEmptyMessage(0);
        }
    }

    private void inflateSendMst(CharSequence msg){
        View sentView = LayoutInflater.from(this).inflate(R.layout.layout_msg_send, null);
        ((TextView) sentView.findViewById(R.id.sendText)).setText(msg);
        ((ImageView) sentView.findViewById(R.id.myFaceImg)).setImageResource(R.drawable.ic_me);
        msgContainer.addView(sentView);
        msgInput.setText(null);
    }

    private void inflateReceivedMsg(CharSequence msg){
        View sentView = LayoutInflater.from(this).inflate(R.layout.layout_msg_receive, null);
        ((TextView) sentView.findViewById(R.id.textReceive)).setText(msg);
        if (contactFaceBitmap != null) {
            ((ImageView) sentView.findViewById(R.id.senderHeadImg)).setImageBitmap(contactFaceBitmap);
        }
        msgContainer.addView(sentView);
    }

    public void back(View v){
        msgDao.closeDB();
        finish();
    }

    @Override
    public void finish() {
        if(getIntent().getIntExtra("p",-1)>0){
            YuXunActivity.LAST_MSG_CHANGED=false;
        }
        setResult(1, getIntent().putExtra("lastMsg", lasMsg));
        super.finish();
    }

    public void lastMsgChange(String lastMsg){
        lasMsg = lastMsg;
        YuXunActivity.LAST_MSG_CHANGED=true;
    }

    public List<String> receiveMsgFromServer(){
        HttpTransportSE ht = new HttpTransportSE(SERVICE_URL);
        SoapObject soapObject = new SoapObject(SERVICE_NAMESPACE, methodName);
        // 添加一个请求参数
        soapObject.addProperty("theRegionCode", "");
        // 使用soap1.1协议创建envelop对象
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(
                SoapEnvelope.VER11);
        envelope.bodyOut = soapObject;
        // 设置与.NET提供的webservice保持较好的兼容性
        envelope.dotNet = true;
        try {
            ht.call(SERVICE_NAMESPACE + methodName, envelope);
            if (envelope.getResponse() != null) {
                // 获取服务器响应返回的SOAP消息
                SoapObject result = (SoapObject) envelope.bodyIn;
                SoapObject detail = (SoapObject) result.getProperty(methodName
                        + "Result");
                // 解析服务器响应的SOAP消息
                return parseProvinceOrCity(detail);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return null;
    }

    // 解析省份或城市
    public static List<String> parseProvinceOrCity(SoapObject detail) {
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < detail.getPropertyCount(); i++) {
            // 解析出每个省份
            result.add(detail.getProperty(i).toString().split(",")[0]);
        }
        return result;
    }
}
