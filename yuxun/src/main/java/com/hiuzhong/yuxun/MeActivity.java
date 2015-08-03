package com.hiuzhong.yuxun;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.hiuzhong.baselib.view.SwitchView;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.application.YuXunApplication;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.dao.MsgCountDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONException;
import org.json.JSONObject;


public class MeActivity extends YuXunActivity {


    private SwitchView switchAutoLogin;
    private SwitchView switchSound;
    private JSONObject myAccount;
    private MessageDbManager msgDao;
    private MsgCountDbManager msgCountDao;
    private WebServiceHelper wsClientContactUs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_me);
        super.onCreate(savedInstanceState);
        ActivityHelper.setTitle(this, getTitle().toString());
        myAccount = ActivityHelper.getMyAccount(this);
        switchAutoLogin = (SwitchView) findViewById(R.id.switchAutoLogin);
        switchSound = (SwitchView) findViewById(R.id.switchSound);
        switchAutoLogin.setOnStateChangedListener(new SwitchView.OnStateChangedListenersss() {
            @Override
            public void onChanged(boolean switchOn) {
                setAutoLogin(switchOn);
            }
        });

        switchSound.setOnStateChangedListener(new SwitchView.OnStateChangedListenersss() {
            @Override
            public void onChanged(boolean switchOn) {
                setSound(switchOn);
            }
        });


        switchAutoLogin.setState(myAccount.optBoolean("autoLogin"));
        switchSound.setState( myAccount.optBoolean("soundOn"));
        ActivityHelper.setTitle(this);
        msgDao = new MessageDbManager(this);
        msgCountDao = new MsgCountDbManager(this);

        wsClientContactUs = WebServiceHelper.createShowMsg(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, int... position) {
                Intent intent= new Intent(MeActivity.this, MsgShowActivity.class);
                intent.putExtra("title","关于我们");
                intent.putExtra("msg",json.optString("data"));
                MeActivity.this.startActivity(intent);
            }
        });
    }

    protected void setAutoLogin(Boolean autoLogin){
        try {
            myAccount.putOpt("autoLogin", autoLogin);
            ActivityHelper.reSaveMyAccount(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    protected  void setSound(boolean soundOn){
        try {
            myAccount.putOpt("soundOn", soundOn);
            ActivityHelper.reSaveMyAccount(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void toChangePwd(View v){
        startActivity(new Intent(this,ChangePwdActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }


    public void logOut(View v){
        YuXunApplication app = (YuXunApplication) getApplication();
        app.closeAllAct();
        startActivity(new Intent(this, LoginActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    public void toBdGrant(View v){
        startActivity(new Intent(this, RequireBdGrantActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }
    public void toCharge(View v){
        startActivity(new Intent(this, ChargeActivity.class).setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
    }
    public void toCleanMsg(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("您确定要清空所有信息 ?")
                .setCancelable(false)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        msgDao.cleanMsg();
                        msgCountDao.clean();
                        Toast.makeText(MeActivity.this,"信息已全部清理",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("否", null);
        builder.create().show();

    }

    public void toContactUs(View v){
        wsClientContactUs.callWs(ActivityHelper.getMyAccount(this).optString("account"),
                ActivityHelper.getMyAccount(this).optString("pwd"), "1");
    }

    @Override
    public void finish() {
        msgDao.closeDB();
        msgCountDao.closeDB();
        super.finish();
    }
}