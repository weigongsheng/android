package com.hiuzhong.yuxun;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.baselib.listener.ImgUploadedListener;
import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.baselib.view.SwitchView;
import com.hiuzhong.baselib.view.UploadImgView;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.application.YuXunApplication;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.dao.MsgCountDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONException;
import org.json.JSONObject;


public class MeActivity extends YuXunActivity implements ImgUploadedListener {
    private SwitchView switchAutoLogin;
    private SwitchView switchSound;
    private JSONObject myAccount;
    private MessageDbManager msgDao;
    private MsgCountDbManager msgCountDao;
    private WebServiceHelper wsClientContactUs;
    protected UploadImgView myHeadImg;

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

        ((TextView)findViewById(R.id.account)).setText(myAccount.optString("account"));
        myHeadImg = (UploadImgView) findViewById(R.id.headImg);
        myHeadImg.parentActivity=this;
        Bitmap img = ImageLoaderUtil.loadFromFile(this, myAccount.optString("faceImgPath"));
        if(img != null){
            myHeadImg.setImageBitmap(img);
        }
        myHeadImg.faceImgFileName=myAccount.optString("account")+".jpg";
        myHeadImg.uploadListener =  this;
        switchAutoLogin.setState(myAccount.optBoolean("autoLogin"));
        switchSound.setState( myAccount.optBoolean("soundOn"));
        ActivityHelper.setTitle(this);
        msgDao = MessageDbManager.getInstance(this);
        msgCountDao = MsgCountDbManager.getInstance(this);

        wsClientContactUs = WebServiceHelper.createShowMsg(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... position) {
                Intent intent= new Intent(MeActivity.this, MsgShowActivity.class);
                intent.putExtra("title","关于我们");
                intent.putExtra("msg",json.optString("data"));
                MeActivity.this.startActivity(intent);
            }
        });
    }

    public void onLoadFinish(Bitmap imgBitMap){
        ImageLoaderUtil.saveBitmapToFile(MeActivity.this, myHeadImg.faceImgFileName, imgBitMap);
        try {
            myAccount.put("faceImgPath", myHeadImg.faceImgFileName);
            ActivityHelper.reSaveMyAccount(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    protected void setAutoLogin(Boolean autoLogin){
        try {
            myAccount.putOpt("autoLogin", autoLogin);
            ActivityHelper.reSaveMyAccount(this);
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        myHeadImg.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
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
                        ActivityHelper.msgChanged();
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
        super.finish();
    }
}
