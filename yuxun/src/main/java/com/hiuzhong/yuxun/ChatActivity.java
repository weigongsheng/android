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
import com.hiuzhong.yuxun.constant.Constants;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.dao.MsgCountDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;
import com.hiuzhong.yuxun.receiver.OnReceiveMsgListener;
import com.hiuzhong.yuxun.service.MsgService;
import com.hiuzhong.yuxun.vo.Contact;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.os.Handler;
import android.widget.Toast;

import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;


public class ChatActivity extends Activity {
public static final String INTENT_PARA_CONTACT="contactAccount";
    public static final int STAT_PULL_START=1;
    public static final int STAT_PULL_ING=2;
    public static final int STAT_PULL_ED=3;
    public static final int STAT_PULL_NULL=4;
    protected TextView msgInput;
    private LinearLayout msgContainer;
    private ScrollView msgScrollView;
    private MessageDbManager msgDao;
    private Bitmap contactFaceBitmap;

    protected Handler handler;
    protected String lasMsg;
    private Contact cant;
    private WebServiceHelper charWsClient;
    private JSONObject myAccount;
    private Handler msgHandler;
    private MsgCountDbManager msgcountDao;
    protected int lastPage=0;
    float startY=0;

    int curStatus=STAT_PULL_NULL;

    int pageSize = 20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        msgInput = (TextView) findViewById(R.id.msgEdit);
        msgContainer = (LinearLayout) findViewById(R.id.msgScrollContainer);
        msgScrollView = (ScrollView) findViewById(R.id.msgScrollView);
        msgDao =   MessageDbManager.getInstance(this);
        msgcountDao =  MsgCountDbManager.getInstance(this);
        initData();
        showLostMsg();
        if(cant.account.length()>=5 && cant.account.length()<=7){
            charWsClient = WebServiceHelper.createSendBdClient(this, new WsCallBack () {
                @Override
                public void whenResponse(JSONObject json,Object ... p) {
                    Toast.makeText(ChatActivity.this,"发送成功",Toast.LENGTH_SHORT).show();
                }
            });

        }else {
            charWsClient = WebServiceHelper.createSendAppClient(this, new WsCallBack() {
                @Override
                public void whenResponse(JSONObject json,Object ... p) {
                    Toast.makeText(ChatActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
                }
            });
        }

        msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Constants.MSG_WHAT_CUR) {
                    showReceivedMsg(msg.getData().getString("msg"));
                    if(!cant.isStrange){
                        msgcountDao.cleanCount(cant.account);
                    }
                }
            }
        };

        myAccount = ActivityHelper.getMyAccount(this);
        MsgService.getCurService().setCurContatListener(new OnReceiveMsgListener() {
            @Override
            public void onMessage(String account, String msg, String time) {
                if (account.equals(cant.account)) {
                    Message m = new Message();
                    m.what = Constants.MSG_WHAT_CUR;
                    Bundle b = new Bundle();
                    b.putString("msg", msg);
                    m.setData(b);
                    msgHandler.sendMessage(m);
                }
            }
        });

        ActivityHelper.initHeadInf(this, cant.nickName == null ? cant.account : cant.nickName);
        msgcountDao.cleanCount(cant.account);
//        msgScrollView.post()
    }

    private void showLostMsg(){
        List<String[]> msgs = msgDao.query(pageSize, 0,cant.account);
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
        ContactsDbManager dao =   ContactsDbManager.getInstance(this);
        cant =dao.queryByAccount(getIntent().getStringExtra(INTENT_PARA_CONTACT));
        if(cant == null){
            cant = Contact.createStranger(getIntent().getStringExtra(INTENT_PARA_CONTACT));
        }else{
          contactFaceBitmap = ImageLoaderUtil.loadFromFile(this, cant.faceImgPath);
        }
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



    public void sendMsg(View v) {
        CharSequence msg = msgInput.getText();
        if (msg != null && ! msg.equals("")) {
            inflateSendMst(msg);
            if(!cant.isStrange){
                msgDao.add(cant.account, MessageDbManager.MSG_TYPE_SEND, msg.toString());
            }
            handler.sendEmptyMessage(0);
            charWsClient.callWs(myAccount.optString("account"), myAccount.optString("pwd"), cant.account, msg.toString());

        }
    }
    public void showReceivedMsg(CharSequence msg) {
        if (msg != null) {
            //TODO
            inflateReceivedMsg(msg);
//            if(!cant.isStrange){
//                msgDao.add(String.valueOf(cant.account), MessageDbManager.MSG_TYPE_RECEIVE, msg.toString());
//            }
            handler.sendEmptyMessage(0);
        }
    }

    private void inflateSendMst(CharSequence msg){
        View sentView = LayoutInflater.from(this).inflate(R.layout.layout_msg_send, null);
        ((TextView) sentView.findViewById(R.id.sendText)).setText(msg);
        ((ImageView) sentView.findViewById(R.id.myFaceImg)).setImageResource(R.drawable.ic_me);
        msgContainer.addView(sentView);
        msgInput.setText(null);
        ActivityHelper.msgChanged();
    }
    private void inflateSendMst(CharSequence msg,int index){
        View sentView = LayoutInflater.from(this).inflate(R.layout.layout_msg_send, null);
        ((TextView) sentView.findViewById(R.id.sendText)).setText(msg);
        ((ImageView) sentView.findViewById(R.id.myFaceImg)).setImageResource(R.drawable.ic_me);
        msgContainer.addView(sentView, index);
        msgInput.setText(null);
    }

    private void inflateReceivedMsg(CharSequence msg){
        View sentView = LayoutInflater.from(this).inflate(R.layout.layout_msg_receive, null);
        ((TextView) sentView.findViewById(R.id.textReceive)).setText(msg);
        if (contactFaceBitmap != null) {
            ((ImageView) sentView.findViewById(R.id.senderHeadImg)).setImageBitmap(contactFaceBitmap);
        }
        msgContainer.addView(sentView);
        ActivityHelper.msgChanged();
    }
    private void inflateReceivedMsg(CharSequence msg,int index){
        View sentView = LayoutInflater.from(this).inflate(R.layout.layout_msg_receive, null);
        ((TextView) sentView.findViewById(R.id.textReceive)).setText(msg);
        if (contactFaceBitmap != null) {
            ((ImageView) sentView.findViewById(R.id.senderHeadImg)).setImageBitmap(contactFaceBitmap);
        }
        msgContainer.addView(sentView,index);
        ActivityHelper.msgChanged();
    }

    public void back(View v){
        finish();
    }

    @Override
    public void finish() {
        if(getIntent().getIntExtra("p",-1)<0){
            YuXunActivity.LAST_MSG_CHANGED=false;
        }
        setResult(1, getIntent().putExtra("lastMsg", lasMsg));
        super.finish();
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

    @Override
    protected void onDestroy() {
        try {
            MsgService.getCurService().setCurContatListener(null);
        }catch (Throwable e){
        }
        super.onDestroy();
    }

    public void loadMore(View view) {
        List<String[]> msgs = msgDao.query(pageSize, lastPage+1,cant.account);
        if(msgs == null){
            return ;
        }
        if(msgs.size()>0){
            ++lastPage;
        }

        for (int i = 0; i <msgs.size(); i++) {
            String[] msg = msgs.get(i);
            if(msg!= null){
                if(MessageDbManager.MSG_TYPE_SEND.equals(msg[3])){
                    inflateSendMst(msg[1],0);
                }else{
                    inflateReceivedMsg(msg[1],0);
                }
            }
        }

    }


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {

        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                    curStatus=STAT_PULL_NULL;
                startY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                    if(ev.getY()-startY>0){
                        if(msgScrollView.getScrollY()==0){
                            curStatus = STAT_PULL_ED;
                        }else{
                            curStatus = STAT_PULL_ING;
                        }
                    }else{
                        curStatus = STAT_PULL_NULL;
                    }
                break;
            case MotionEvent.ACTION_UP:
                if(curStatus == STAT_PULL_ED){
                    loadMore(null);
                }
            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }
}
