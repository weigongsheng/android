package com.hiuzhong.yuxun;

import android.app.Activity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hiuzhong.baselib.view.SwitchView;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONObject;


public class ChangePwdActivity extends Activity {

    protected EditText inputCurPwd;
    protected EditText inputNewPwd;
    protected EditText inputRepPwd;
    protected WebServiceHelper cpwdClient;
    protected SwitchView swShowPwd;
    private View laodView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_pwd);
        inputCurPwd = (EditText) findViewById(R.id.inputCurPwd);
        inputNewPwd = (EditText) findViewById(R.id.inputNewPwd);
        inputRepPwd = (EditText) findViewById(R.id.inputConfirmPwd);
        swShowPwd = (SwitchView) findViewById(R.id.switchShowPwd);
        laodView = findViewById(R.id.loadingLayoutInclude);
        cpwdClient = WebServiceHelper.createChangePwdClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, int... position) {
                Toast.makeText(ChangePwdActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void whenError() {
                laodView.setVisibility(View.GONE);
            }

            @Override
            public void whenFail(JSONObject json) {
                laodView.setVisibility(View.GONE);
            }
        });

        swShowPwd.setOnStateChangedListener(new SwitchView.OnStateChangedListenersss() {
            @Override
            public void onChanged(boolean switchOn) {
                int type = switchOn?InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD:InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                inputCurPwd.setInputType(type);
                inputNewPwd.setInputType(type);
                inputRepPwd.setInputType(type);
            }
        });
        ActivityHelper.initHeadInf(this);
    }

    public void submit(View v){
        if(inputCurPwd.getText() == null && "".equals(inputCurPwd.getText().toString())){
            Toast.makeText(this,"请输入当前密码",Toast.LENGTH_SHORT).show();
        }else if(inputNewPwd.getText() == null && "".equals(inputNewPwd.getText().toString())){
            Toast.makeText(this,"请输入新密码",Toast.LENGTH_SHORT).show();
        }else if(!inputNewPwd.getText().toString().equals(inputRepPwd.getText().toString())){
            Toast.makeText(this,"密码不一致",Toast.LENGTH_SHORT).show();
        }else{
            laodView.setVisibility(View.VISIBLE);
            cpwdClient.callWs(ActivityHelper.getMyAccount(this).optString("account"),inputCurPwd.getText().toString(),inputNewPwd.getText().toString());
        }
    }


}
