package com.hiuzhong.yuxun;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

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

    private View[] grantViewList = new View[3];
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

        grantListClient = WebServiceHelper.createGrantListClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, int... position) {
                showGrant(json.optJSONObject("data").optJSONArray("GrantBDList"));
            }
        }).callWs(myAccount.optString("account"), myAccount.optString("pwd"));
        cancelClient = WebServiceHelper.createGrantCancelClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, int... position) {
                grantViewList[position[0]].setVisibility(View.GONE);
            }
        });
        requestClient = WebServiceHelper.createGrantRegClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, int... position) {
                Toast.makeText(RequireBdGrantActivity.this, json.optString("tip"), Toast.LENGTH_LONG).show();
            }
        });
        grantViewList[0] = findViewById(R.id.layoutCancel1);
        grantViewList[1] = findViewById(R.id.layoutCalcel2);
        grantViewList[2] = findViewById(R.id.layoutCalcel3);
        ActivityHelper.initHeadInf(this);
    }

    protected void showGrant(JSONArray jsonArray) {
        if(jsonArray == null || jsonArray.length()<1){
            return;
        }
        grantBdInfo = jsonArray;
        initGrantView( grantViewList[0],R.id.tipBdNum1,R.id.cancelGran1,jsonArray.optJSONObject(0),0);
        initGrantView( grantViewList[1],R.id.tipBdNum2,R.id.cancelGran2,jsonArray.optJSONObject(1),1);
        initGrantView( grantViewList[2],R.id.tipBdNum3,R.id.cancelGran3,jsonArray.optJSONObject(2),2);


    }

    private void initGrantView(View view, int tipBdNum1, int cancelGran1, JSONObject json,int index) {
        if(json == null){
            return;
        }
        view.setVisibility(View.VISIBLE);
        ((TextView)view.findViewById(tipBdNum1)).setText("北斗号:" + json.optString("BdNum"));
        ((Button)view.findViewById(cancelGran1)).setTag(index);
    }


    public void cancelGrant(View v){
//        curIndex = (int) v.getTag();
        cancelClient.callWs((int) v.getTag(), 0, myAccount.optString("account"), myAccount.optString("pwd"), grantBdInfo.optJSONObject(curIndex).optString("BdNum"));
         //todo
    }

    public void reqGrant(View v){
        String reqNum = reqInput.getText().toString();
        if(reqNum == null || "".equals(reqNum.toString())){
            Toast.makeText(this,"请输入北斗",Toast.LENGTH_SHORT).show();
        }
        requestClient.callWs(myAccount.optString("account"), myAccount.optString("pwd"),reqNum);
    }



}
