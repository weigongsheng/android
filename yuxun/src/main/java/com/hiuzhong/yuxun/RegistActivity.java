package com.hiuzhong.yuxun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.hiuzhong.yuxun.application.YuXunApplication;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONObject;

import java.util.Timer;



public class RegistActivity extends Activity {
    private EditText phoneNum;
    private View progressBarLayout;
    private final Timer timer = new Timer();

    private int type;
    private WebServiceHelper showMsgClient;

    String curVc;


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
                public void whenResponse(JSONObject json,Object ... p) {
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
        showMsgClient.callWs("2");
    }

    protected void sentMsg(){
        progressBarLayout.setVisibility(View.VISIBLE);
        curVc = WebServiceHelper.sendVc(phoneNum.getText().toString(), this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... extraPara) {
                Intent intent = new Intent(RegistActivity.this,ValidCodeActivity.class);
                intent.putExtra("phoneNum",phoneNum.getText().toString());
                intent.putExtra("type",type);
                intent.putExtra("vc",(String)extraPara[0]);
                startActivity(intent);
                finish();
            }

            @Override
            public void whenError() {
                progressBarLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void whenFail(JSONObject json) {
                progressBarLayout.setVisibility(View.VISIBLE);
            }
        });

    }

}
