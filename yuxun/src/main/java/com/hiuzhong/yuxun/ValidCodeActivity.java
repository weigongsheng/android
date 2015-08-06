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
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;



public class ValidCodeActivity extends Activity {

    private  Timer timer ;
    private Button acqVcBtn;
    private Handler handler;
    protected final  int MSG_SEND_VC=1;

    private EditText textVc;
    private TextView phoneNum;
    private TimerTask task;
    int count=60;
    String curVc ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_valid_code);
        phoneNum =(TextView)findViewById(R.id.phoneNumTip);
        phoneNum.setText(getIntent().getStringExtra("phoneNum"));
        textVc = (EditText) findViewById(R.id.textVc);
        acqVcBtn = (Button) findViewById(R.id.acqVcBtn);
        ActivityHelper.initHeadInf(this, "填写验证码");
        curVc = getIntent().getStringExtra("vc");
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
                }

            }
        };
        showDisCount();
    }

    protected void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    public void toNext(View v) {
        if (textVc.getText() == null || textVc.getText().length() < 1) {
            showMsg("请输入验证码");
            return;
        }
       if(!curVc.equals(textVc.getText().toString())){
           showMsg("验证码错误");
           return;
       }
        toSetPwd();
    }


    public void sentMsg(View v){
        acqVcBtn.setClickable(false);
        showDisCount();
        curVc = WebServiceHelper.sendVc(phoneNum.getText().toString(), this,null);
    }
    private void showDisCount(){
        count =30;
        textVc.setText("");
        task = new TimerTask() {
            public void run() {
                Message message = new Message();
                message.what = MSG_SEND_VC;
                handler.sendMessage(message);
            }
        };
        if(timer != null){
            timer.cancel();
        }
        timer = new Timer();
        timer.schedule(task, 1000, 1000);
    }

    public void toSetPwd(){
        int type = getIntent().getIntExtra("type",0);
        if(type ==1){
            Toast.makeText(this,"密码已经发送到您的手机",Toast.LENGTH_SHORT).show();
            this.finish();
            return;
        }
        Intent intent = new Intent(this,SettingPwdActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra("phoneNum", phoneNum.getText().toString());
        startActivity(intent);
        this.finish();

    }
}
