package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.yuxun.application.YuXunApplication;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;
import com.hiuzhong.yuxun.service.MsgService;

import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

public class LoginActivity extends Activity {
    TextView userName;
    TextView pwd;
    Handler handler;
    private WebServiceHelper loginClient;
    private View laodingView;
    ImageView curHeadImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        userName = (TextView) findViewById(R.id.userName);
        pwd = (TextView) findViewById(R.id.userPwd);
        ActivityHelper.setTitle(this);
        ((YuXunApplication)getApplication()).addRegistAct(this);
        handler = WebServiceHelper.createMsgHandler(this);
        laodingView = findViewById(R.id.loadingLayoutInclude);
        curHeadImg = (ImageView) findViewById(R.id.myHeadView);
        JSONObject myAccount = ActivityHelper.getMyAccount(this);
        if(myAccount != null){
            if( myAccount.optString("faceImgPath") != null){
                try {
                curHeadImg.setImageBitmap(ImageLoaderUtil.loadFromFile(this,myAccount.optString("faceImgPath")));
                }catch (Exception e){}
            }
            userName.setText(myAccount.optString("account"));
        }

        loginClient = WebServiceHelper.createLoginClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json,Object ... p) {
                Toast.makeText(LoginActivity.this,json.optString("tip"),Toast.LENGTH_SHORT).show();
                laodingView.setVisibility(View.GONE);
                ActivityHelper.saveMyAccount(LoginActivity.this,"",userName.getText().toString(),pwd.getText().toString());
                toListMsg();
            }

            @Override
            public void whenError() {

            }

            @Override
            public void whenFail(JSONObject json) {
                laodingView.setVisibility(View.GONE);
            }
        });
    }


    public void login(View v){
        laodingView.setVisibility(View.VISIBLE);loginClient.callWs(userName.getText().toString(), pwd.getText().toString());
    }

    protected  void toListMsg(){
        Intent intent =new Intent(this,ChatListActivity.class);
        intent.putExtra("b_index", 0);
        startActivity(intent);
        startService(new Intent(getApplicationContext(),MsgService.class));
        finish();
    }
    public  void toRegist(View v){
        startActivity(new Intent(this,RegistActivity.class));
    }
    
    public  void toRePwd(View v){
        startActivity(new Intent(this,RegistActivity.class).putExtra("type",1));
    }
}
