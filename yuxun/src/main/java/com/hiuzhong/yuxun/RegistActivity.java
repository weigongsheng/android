package com.hiuzhong.yuxun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hiuzhong.yuxun.application.YuXunApplication;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;


public class RegistActivity extends Activity {
    private EditText phoneNum;
    private Handler handler;
    private View progressBarLayout;
    private final Timer timer = new Timer();

    protected final  int MSG_SEND_VC=1;
    protected final  int MSG_SEND_SUCCESS=2;
    protected final  int MSG_ACQ_VC=3;
    protected final  int MSG_ERROR=4;
    private int type;
    private WebServiceHelper showMsgClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((YuXunApplication)getApplication()).addRegistAct(this);
        setContentView(R.layout.activity_regist);
        ActivityHelper.initHeadInf(this);
        progressBarLayout =findViewById(R.id.progressBarLayout);
        phoneNum =(EditText)findViewById(R.id.phoneNum);
        type = getIntent().getIntExtra("type",0);
        String title = "用户注册";
        if(type ==1){
            title = "找回密码";
            findViewById(R.id.actContract).setVisibility(View.GONE);
            findViewById(R.id.tipTitleContract).setVisibility(View.GONE);
        }
        ActivityHelper.initHeadInf(this, title);
        initSMS();
          handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_SEND_SUCCESS:{
                        progressBarLayout.setVisibility(View.GONE);
                        toCheckVc();
                        break;
                    }
                    case MSG_ACQ_VC:{
                        progressBarLayout.setVisibility(View.GONE);
                        showMsg("验证码验证通过");
                        break;
                    }
                    case MSG_ERROR:{
                        progressBarLayout.setVisibility(View.GONE);
                        Toast.makeText(RegistActivity.this,msg.getData().getString("tip"),Toast.LENGTH_LONG).show();
                        break;
                    }
                }

            }
        };
    }

    private void initSMS() {
        SMSSDK.initSDK(this, getResources().getString(R.string.smsAppKey), getResources().getString(R.string.smsAppSecret));
        EventHandler eh=new EventHandler(){
            @Override
            public void afterEvent(int event, int result, Object data) {
                if (result == SMSSDK.RESULT_COMPLETE) {
                    if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                        Message message = new Message();
                        message.what = MSG_ACQ_VC;
                        handler.sendMessage(message);
                    }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){
                        Message message = new Message();
                        message.what =MSG_SEND_SUCCESS ;
                        handler.sendMessage(message);
                    }else if (event ==SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){
                        //返回支持发送验证码的国家列表
                    }
                }else{
                    Throwable e = ((Throwable) data);;
                    Message message = new Message();
                    message.what = MSG_ERROR;
                    Bundle b = new Bundle();
                    try {
                        JSONObject json = new JSONObject(e.getMessage());
                    b.putString("tip", json.optString("detail"));
                    message.setData(b);
                    handler.sendMessage(message);
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        };
        SMSSDK.registerEventHandler(eh); //注册短信回调
    }

protected void showMsg(String msg){
    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
}

    public void toNext(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if(phoneNum.getText() == null || phoneNum.getText().length()<11){
            showMsg("请填写正确的手机号");
            return;
        }
        builder.setTitle("确认手机号码");
        builder.setMessage("我们将发送短信验证码到下面号码 \n"+phoneNum.getText()+"")
                .setCancelable(false)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sentMsg();
                    }
                })
                .setNegativeButton("否", null);
        builder.create().show();

    }

    public void toShowMsg(View view){
        if(showMsgClient == null){
            showMsgClient =WebServiceHelper.createShowMsg(this, new WsCallBack() {
                @Override
                public void whenResponse(JSONObject json) {
                    Intent intent= new Intent(RegistActivity.this, MsgShowActivity.class);
                    intent.putExtra("title","用户协议");
                    intent.putExtra("msg",json.optString("data"));
                    RegistActivity.this.startActivity(intent);
                }

                @Override
                public void whenError() {

                }

                @Override
                public void whenFail(JSONObject json) {

                }
            });
        }
        showMsgClient.callWs("66621979360", "11", "2");
    }

    protected void sentMsg(){
        progressBarLayout.setVisibility(View.VISIBLE);
        SMSSDK.getVerificationCode("86", phoneNum.getText().toString());
//        toCheckVc();
    }


    @Override
    public void finish() {
        SMSSDK.unregisterAllEventHandler();
        super.finish();
    }

    public void toCheckVc(){
        Intent intent = new Intent(this,ValidCodeActivity.class);
        intent.putExtra("phoneNum",phoneNum.getText().toString());
        intent.putExtra("type",type);
        startActivity(intent);
        finish();
    }

}
