package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.application.YuXunApplication;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONObject;


public class SettingPwdActivity extends Activity {

    private TextView tipPhoneNum;
    private TextView tipAccount;
    private EditText repPwd;
    private EditText pwdText;
    int type;
    private Handler handler;
    private Handler wsHandler;
    String oldPwd;
    private WebServiceHelper queryClient;
    private WebServiceHelper changPwdClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_pwd);
        tipPhoneNum = (TextView) findViewById(R.id.labelPhoneNum);
        tipAccount = (TextView) findViewById(R.id.labelAccount);
        repPwd = (EditText) findViewById(R.id.repPwd);
        pwdText = (EditText) findViewById(R.id.pwdText);
        String pn = getIntent().getStringExtra("phoneNum");
        type = getIntent().getIntExtra("type",0);
        tipPhoneNum.setText(pn);
        tipAccount.setText("666" + pn.substring(3));
        ActivityHelper.initHeadInf(this, "设置密码");
        handler = WebServiceHelper.createMsgHandler(this);
        wsHandler = WebServiceHelper.createMsgHandler(this);

        if(type ==1){
           queryClient = WebServiceHelper.createQueryPwdClient(this, new WsCallBack() {
               @Override
               public void whenResponse(JSONObject json,Object ... p) {
                   oldPwd = json.optString("data");
               }
           }).callWs(tipAccount.getText().toString());
            changPwdClient =  WebServiceHelper.createChangePwdClient(this, new WsCallBack() {
                @Override
                public void whenResponse(JSONObject json,Object ... p) {
                    ActivityHelper.saveMyAccount(SettingPwdActivity.this, tipPhoneNum.getText().toString(), tipAccount.getText().toString(), pwdText.getText().toString());
                    toListMsg();
                }
            });
        }
    }



    public void toNex(View v) {
        if (pwdText.getText() == null || pwdText.getText().length() < 1) {
            Toast.makeText(this, "请填入密码", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pwdText.getText().toString().equals(repPwd.getText().toString())) {
            Toast.makeText(this, "密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }
        WebServiceHelper.createRegistClient(this,new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json,Object ... p) {
                Toast.makeText(SettingPwdActivity.this, json.optString("tip"), Toast.LENGTH_SHORT).show();
                ActivityHelper.saveMyAccount(SettingPwdActivity.this, tipPhoneNum.getText().toString(), tipAccount.getText().toString(), pwdText.getText().toString());
                toListMsg();
            }
        }).callWs(tipAccount.getText().toString().trim(), pwdText.getText().toString().trim(), tipPhoneNum.getText().toString().trim());
    }

    private final void toListMsg() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("b_index", 0);
        startActivity(intent);
        finish();
        ((YuXunApplication) getApplication()).finishReg();
    }

}
