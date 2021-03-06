package com.hiuzhong.yuxun;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.dao.MsgCountDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;
import com.hiuzhong.yuxun.vo.Contact;
import com.hp.hpl.sparta.xpath.ThisNodeTest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class ContactDetailActivity extends Activity {
    public static  final int MSG_READY_QUERY =123;
    private ImageView faceView;
    private Contact contact;
    //    private Bitmap contactFaceBitmap;
    private TextView nickName;
    private TextView account;
    protected Handler handler;
    protected TextView banlenceText;
    protected WebServiceHelper queryClient;
    private JSONObject myAccount;
    protected String qId;
    private WebServiceHelper questClient;
    private MessageDbManager msgDao;
    private MsgCountDbManager msgCountDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_detail);
        faceView = (ImageView) findViewById(R.id.detailContactHeadImg);
        nickName = (TextView) findViewById(R.id.detaillName);
        account = (TextView) findViewById(R.id.detailAccount);
        msgDao =   MessageDbManager.getInstance(this);
        msgCountDao =   MsgCountDbManager.getInstance(this);
        initData();
        if(contact.isBdAccount()){
            findViewById(R.id.showPosLayout).setVisibility(View.VISIBLE);
            findViewById(R.id.showBalance).setVisibility(View.VISIBLE);
            banlenceText = (TextView) findViewById(R.id.textBalance);
        }

        myAccount = ActivityHelper.getMyAccount(this);
         queryClient = WebServiceHelper.createQueryBalanceClient(this, new WsCallBack() {
             @Override
             public void whenResponse(JSONObject json,Object ... p) {
                 try {
                     banlenceText.setText(json.getJSONObject("data").optString("BalanceContent"));
                 } catch (JSONException e) {
                     e.printStackTrace();
                 }
             }
         });

        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                 if(msg.what == MSG_READY_QUERY){
                     queryClient.callWs(myAccount.optString("account"),myAccount.optString("pwd"),qId);
                 }
            }
        };
        questClient = WebServiceHelper.createQueryBalanceReqClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json,Object ... p) {
                qId = json.optJSONObject("data").optString("BalanceID");
                handler.sendEmptyMessageDelayed(MSG_READY_QUERY,4*1000);
            }
        });
    }

    private void initData() {
        ContactsDbManager dao =  ContactsDbManager.getInstance(this);
        contact = dao.queryById(getIntent().getStringExtra("contactId"));
        Bitmap img = ImageLoaderUtil.loadFromFile(this, contact.faceImgPath);
        if(img != null){
            faceView.setImageBitmap(ImageLoaderUtil.loadFromFile(this, contact.faceImgPath));
        }
        nickName.setText(contact.nickName);
        account.setText(contact.account);
    }

    public void back(View v) {
        finish();
    }

    public void deleteContact(View v) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("您确定要删除联系人 \n"+contact.nickName+" ?")
                .setCancelable(false)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        deleteContact();
                    }
                })
                .setNegativeButton("否", null);
         builder.create().show();
    }

    protected void deleteContact(){
        ContactsDbManager dao = ContactsDbManager.getInstance(this);
        dao.deleteContact(contact.account);
        ImageLoaderUtil.deletImg(this, contact.faceImgPath);
        Toast.makeText(this, "联系人已删除", Toast.LENGTH_SHORT).show();
        ActivityHelper.contactChanged();
        finish();
    }


    public void sendMsg(View v){
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_PARA_CONTACT, contact.account);
        startActivity(intent);
        finish();
    }

    public void showPositon(View v){
        WebServiceHelper.createGetPosiClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... p) {
                JSONArray ja = json.optJSONObject("data").optJSONArray("BDPostion");
                if (ja.length() < 1) {
                    Toast.makeText(ContactDetailActivity.this, "没有可显示的位置信息", Toast.LENGTH_SHORT).show();
                    return;
                }else{
                    Intent intent =new Intent(ContactDetailActivity.this,ShowPositionActivity.class).putExtra("account",contact.account).putExtra("nickName", contact.nickName);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }

            }
        }).callWs(ActivityHelper.getMyAccount(this).optString("account"),ActivityHelper.getMyAccount(this).optString("pwd"), contact.account);

}

    public void showBalance(View v){
        v.setClickable(false);
        questClient.callWs(myAccount.optString("account"), myAccount.optString("pwd"), contact.account);
    }

    public void toCleanMsg(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("您确定要清空所有信息与"+contact.nickName+"的聊天信息?")
                .setCancelable(false)
                .setPositiveButton("是", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        msgDao.cleanMsg(contact.account);
                        msgCountDao.clean(contact.account);
                        ActivityHelper.msgChanged();
                        Toast.makeText(ContactDetailActivity.this,"信息已全部清理",Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("否", null);
        builder.create().show();

    }

    public void toEditContact(View view) {
        Intent intent = new Intent(this,EditContactActivity.class);
        intent.putExtra("contactId",""+contact.id);
        startActivity(intent);
        finish();
    }
}
