package com.hiuzhong.yuxun;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;
import com.hiuzhong.yuxun.vo.Contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class RequireBdGrantActivity extends Activity {

    private JSONObject myAccount;
    private JSONArray grantBdInfo;
    private WebServiceHelper grantListClient;
    private WebServiceHelper cancelClient;
    private WebServiceHelper requestClient;
    private EditText reqInput;

    protected  int curIndex;

    ContactsDbManager contactDao;

//    private View[] grantViewList = new View[3];
    private LinearLayout msgContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_require_bd_grant);
        myAccount = ActivityHelper.getMyAccount(this);
        reqInput = (EditText) findViewById(R.id.reqEditText);
        grantBdInfo = myAccount.optJSONArray("bdGrant");
        if(grantBdInfo == null){
            try {
                grantBdInfo = new JSONArray("[]");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        contactDao =   ContactsDbManager.getInstance(this);
        grantListClient = WebServiceHelper.createGrantListClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... position) {
                showGrant(json.optJSONObject("data").optJSONArray("GrantBDList"));
            }
        }).callWs(myAccount.optString("account"), myAccount.optString("pwd"));
        cancelClient = WebServiceHelper.createGrantCancelClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... extra) {
                msgContainer.removeView((View) extra[0]);
            }
        });
        requestClient = WebServiceHelper.createGrantRegClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... position) {
                Toast.makeText(RequireBdGrantActivity.this, json.optString("tip"), Toast.LENGTH_LONG).show();
            }
        });
        ActivityHelper.initHeadInf(this);
        msgContainer = (LinearLayout) findViewById(R.id.msgScrollContainer);
    }

    protected void showGrant(JSONArray jsonArray) {
        if(jsonArray == null || jsonArray.length()<1){
            return;
        }
//        grantBdInfo = jsonArray;
//        initGrantView( grantViewList[0],R.id.tipBdNum1,R.id.cancelGran1,jsonArray.optJSONObject(0),0);
//        initGrantView( grantViewList[1],R.id.tipBdNum2,R.id.cancelGran2,jsonArray.optJSONObject(1),1);
//        initGrantView(grantViewList[2], R.id.tipBdNum3, R.id.cancelGran3, jsonArray.optJSONObject(2), 2);
        for (int i = 0,j=jsonArray.length(); i <j ; i++) {
            inflateReceivedMsg(jsonArray.optJSONObject(i).optString("BdNum"));
        }
    }

//    private void initGrantView(View view, int tipBdNum1, int cancelGran1, JSONObject json,int index) {
//        if(json == null){
//            return;
//        }
//        view.setVisibility(View.VISIBLE);
//        ((TextView)view.findViewById(tipBdNum1)).setText("北斗号:" + json.optString("BdNum"));
//        ((Button)view.findViewById(cancelGran1)).setTag(index);
//    }


    public void cancelGrant(View v){
//        msgContainer.removeView((View) v.getParent());
        cancelClient.callWs(myAccount.optString("account"), myAccount.optString("pwd"), v.getTag().toString(), v.getParent());
         //todo
    }

    public void reqGrant(View v){
        String reqNum = reqInput.getText().toString();
        if(reqNum == null || "".equals(reqNum.toString())){
            Toast.makeText(this,"请输入北斗",Toast.LENGTH_SHORT).show();
        }
        requestClient.callWs(myAccount.optString("account"), myAccount.optString("pwd"),reqNum);
    }

    private void inflateReceivedMsg(String bdNum){
        View sentView = LayoutInflater.from(this).inflate(R.layout.layout_grant, null);
        ((TextView) sentView.findViewById(R.id.tipBdNum)).setText("北斗号:" + bdNum);
        sentView.findViewById(R.id.cancelGran).setTag(bdNum);
        msgContainer.addView(sentView);
    }
    


}
