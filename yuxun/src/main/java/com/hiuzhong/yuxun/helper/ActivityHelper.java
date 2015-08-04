package com.hiuzhong.yuxun.helper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.hiuzhong.yuxun.MsgShowActivity;
import com.hiuzhong.yuxun.R;

import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by gongsheng on 2015/7/25.
 */
public class ActivityHelper {
    public static boolean msgChanged=false;
    public static boolean contactChange=false;

    public static void msgChanged(){
        msgChanged = true;
    }
    public static void contactChanged(){
        contactChange = true;
    }

    public static JSONObject myAccount=null;
    public static void setTitle(Activity act, String... title) {
        TextView t = (TextView) act.findViewById(R.id.head_title_text);
        if (t != null) {
            t.setText(title == null || title.length < 1 ? act.getTitle() : title[0]);
        }
    }

    public static void showBack(final Activity act, View.OnClickListener listener) {
        ImageButton btn = (ImageButton) act.findViewById(R.id.backImgBtn);
        if (btn != null) {
            btn.setVisibility(View.VISIBLE);
            if (listener == null) {
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        act.finish();
                    }
                });
            } else {
                btn.setOnClickListener(listener);
            }
        }

    }

    public static void initHeadInf(Activity act, String... titile) {
        setTitle(act, titile);
        showBack(act, null);
    }

    public static void showMsgAct(Activity act, String title, String msg) {
        Intent intent = new Intent(act, MsgShowActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("msg", msg);
        act.startActivity(intent);
    }

    public static final void saveMyAccount(Activity sp, String phone, String account, String pwd) {
        if(myAccount == null){
            myAccount = getMyAccount(sp);
            if(myAccount == null){
                myAccount = new JSONObject();
            }
        }
        try {
            myAccount.put("phone", phone);
            myAccount.put("account", account);
            myAccount.put("pwd", pwd);
            sp.getSharedPreferences("yuxun", sp.MODE_PRIVATE).edit().putString("myAccount", myAccount.toString()).commit();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static final void reSaveMyAccount(Context sp) {
        sp.getSharedPreferences("yuxun", sp.MODE_PRIVATE).edit().putString("myAccount", getMyAccount(sp).toString()).commit();
    }

    public static JSONObject getUserInfo(Context cnt){
        try {
            return new JSONObject(cnt.getSharedPreferences("yuxun", Context.MODE_PRIVATE).getString("myAccount", ""));
        } catch (JSONException e) {
            return null;
        }
    }

    public static final JSONObject getMyAccount(Context sp) {
        try {
            if(myAccount == null){
                myAccount=new JSONObject(sp.getSharedPreferences("yuxun", sp.MODE_PRIVATE).getString("myAccount", null));
            }
            return myAccount;
        } catch (Exception e) {
            return null;
        }
    }


}

