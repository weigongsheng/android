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


public class MainActivity extends Activity {

    protected static final String TAG = "MainActivity";

    protected static final int NAET_FAIL=0XF12;
    protected static final int INIT_FINISH=0XF01;
    protected static final int FETCH_DATA_FAIL=0XF03;

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
        if(msg.getData() != null && msg.getData().getString("json") != null){

        }
        startActivity(new Intent(MainActivity.this, ChatListActivity.class).putExtra("b_index",0));
        Log.i(TAG, "loadMainUi");
        finish();
    }
}
