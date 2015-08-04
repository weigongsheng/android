package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.hiuzhong.baselib.util.WebUtil;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;
import com.hiuzhong.yuxun.service.MsgService;

import org.json.JSONObject;


public class MainActivity extends Activity {

    protected static final String TAG = "MainActivity";

    protected static final int NAET_FAIL=0XF12;
    protected static final int INIT_FINISH=0XF01;
    protected static final int FETCH_DATA_FAIL=0XF03;

    private WebServiceHelper wsClient;

    private Handler handler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case NAET_FAIL:
                    Toast.makeText(MainActivity.this, "无可用网络", Toast.LENGTH_LONG).show();
                    loadMainUi(msg);
                    break;
                case FETCH_DATA_FAIL:
                    Toast.makeText(MainActivity.this, "无法访问服务器", Toast.LENGTH_LONG).show();
                    loadMainUi(msg);
                    break;
                case INIT_FINISH:
                    loadMainUi(msg);
                    break;
                default:
                    break;
            }
        }
    };

    private AlphaAnimation aa;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        RelativeLayout relWelcomeImg = (RelativeLayout) findViewById(R.id.wellcomLayout);
        aa = new AlphaAnimation(0.2f, 1.0f);
        aa.setDuration(2000);
        relWelcomeImg.setAnimation(aa);

        new Thread() {
            @Override
            public void run() {
                try {
                    if(!WebUtil.isNetworkConnected(MainActivity.this)){
                        Thread.sleep(2000);
                        handler.sendEmptyMessage(NAET_FAIL);
                        return;
                    }
                        requiredLoad(this,INIT_FINISH);
                } catch (Exception e) {
                    requiredLoad(this, FETCH_DATA_FAIL);
                }
            }
        }.start();
    }

    protected void requiredLoad(Thread rThread,int msg){
        try {
            rThread.sleep(aa.computeDurationHint());
        } catch (InterruptedException e) {

        }
        handler.sendEmptyMessage(msg);
    }

    private void loadMainUi(Message msg) {
        JSONObject myAccount = ActivityHelper.getMyAccount(this);
        if(myAccount != null && myAccount.optBoolean("autoLogin")){
            WebServiceHelper.createLoginClient(this, new WsCallBack() {
                @Override
                public void whenResponse(JSONObject json, int... position) {
                    toList();
                }
                @Override
                public void whenError() {
                    toLogin();
                }

                @Override
                public void whenFail(JSONObject json) {
                    toLogin();
                }
            }).callWs(myAccount.optString("account"),myAccount.optString("pwd"));
        }else{
            toLogin();
        }

    }

    protected  void toList(){
        Intent intent =new Intent(this,ChatListActivity.class);
        intent.putExtra("b_index", 0);
        startActivity(intent);
        startService(new Intent(getApplicationContext(),MsgService.class));
        finish();
    }

    protected void toLogin(){
        startActivity(new Intent(MainActivity.this, LoginActivity.class));
        Log.i(TAG, "loadMainUi");
        finish();
    }
}
