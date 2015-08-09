package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONObject;

import java.util.Timer;
import java.util.TimerTask;




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
                            acqVcBtn.setText("重新发送" + count );
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
        startDisCount();
    }

    protected void showMsg(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    public void toNext(View v) {
        if (textVc.getText() == null || textVc.getText().length() < 1) {
            showMsg("请输入验证码");
            return;
        }
        Log.i("yuxun.vc",curVc+" == "+textVc.getText().toString());
       if(!curVc.equals(textVc.getText().toString())){
           showMsg("验证码错误");
           return;
       }else{
           Log.w("vc","vc error "+textVc.getText().toString());
       }
        toSetPwd();
    }


    public void sentMsg(View v){
        startDisCount();
        curVc = WebServiceHelper.sendVc(phoneNum.getText().toString(), this,null);
    }
    private void startDisCount(){
        count =30;
        textVc.setText("");
        acqVcBtn.setClickable(false);
        if(task !=null){
            task.cancel();
        }
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
            WebServiceHelper.createQueryPwdClient(this, new WsCallBack() {
                @Override
                public void whenResponse(JSONObject json, Object... extraPara) {
                    Toast.makeText(ValidCodeActivity.this, "密码已经发送到您的手机", Toast.LENGTH_SHORT).show();
                    ValidCodeActivity.this.finish();
                }
            }).callWs("666"+phoneNum.getText().toString().substring(3));
            return;
        }
        Intent intent = new Intent(this,SettingPwdActivity.class);
        intent.putExtras(getIntent());
        intent.putExtra("phoneNum", phoneNum.getText().toString());
        startActivity(intent);
        this.finish();

    }
}
