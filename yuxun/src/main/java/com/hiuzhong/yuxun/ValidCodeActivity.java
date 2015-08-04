package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.helper.ActivityHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;



public class ValidCodeActivity extends Activity {

    private final Timer timer = new Timer();
    private Button acqVcBtn;
//    private TimerTask task;
    private Handler handler;
    protected final  int MSG_SEND_VC=1;
    protected final  int MSG_SEND_SUCCESS=2;
    protected final  int MSG_ACQ_VC=3;
    protected final  int MSG_ERROR=4;
    private EditText textVc;
    private TextView phoneNum;
    private TimerTask task;
    int count=60;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valid_code);
        phoneNum =(TextView)findViewById(R.id.phoneNumTip);
        phoneNum.setText(getIntent().getStringExtra("phoneNum"));
        textVc = (EditText) findViewById(R.id.textVc);
        acqVcBtn = (Button) findViewById(R.id.acqVcBtn);
        ActivityHelper.initHeadInf(this, "填写验证码");
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case MSG_SEND_VC:{
                        if(--count>=1){
                            acqVcBtn.setText("重新发送 " + count+"秒" );
                        }else{
                            timer.cancel();
                            acqVcBtn.setClickable(true);
                            acqVcBtn.setText("重新发送");
                        }
                        break;
                    }
                    case MSG_SEND_SUCCESS:{
                        showMsg("验证发送成功");
                        break;
                    }
                    case MSG_ACQ_VC:{
                        showMsg("验证码验证通过");
                        toSetPwd();
                        break;
                    }
                    case MSG_ERROR:{
                        Toast.makeText(ValidCodeActivity.this, msg.getData().getString("tip"), Toast.LENGTH_LONG).show();
                        break;
                    }
                }

            }
        };
        initSMS();
    }

    protected void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
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

    public void toNext(View v) {
        if (textVc.getText() == null || textVc.getText().length() < 1) {
            showMsg("请输入验证码");
            return;
        }
        SMSSDK.submitVerificationCode("86", phoneNum.getText().toString(), textVc.getText().toString());
//        toSetPwd();
    }


    protected void sentMsg(View v){
        count =60;
        acqVcBtn.setClickable(false);
        SMSSDK.getVerificationCode("86", phoneNum.getText().toString());
        task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = MSG_SEND_VC;
                handler.sendMessage(message);
            }
        };
        timer.schedule(task, 1000, 1000);
    }

    public void toSetPwd(){
        int type = getIntent().getIntExtra("type",0);
        if(type ==1){
            this.finish();
            return;
        }
        Intent intent = new Intent(this,SettingPwdActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra("phoneNum",phoneNum.getText().toString());
        startActivity(intent);

    }
}
