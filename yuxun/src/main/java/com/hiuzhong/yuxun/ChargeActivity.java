package com.hiuzhong.yuxun;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONObject;


public class ChargeActivity extends Activity {

    protected WebServiceHelper chargeReqClient;
    protected WebServiceHelper confirmChargeClient;
    protected String myAccount;
    protected String myPwd;
    protected View loadingView;
    EditText inputBdNum;
    EditText inputChargeCode;
    private TextView resultTip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_charge);
        ActivityHelper.initHeadInf(this);
        myAccount = ActivityHelper.getMyAccount(this).optString("account");
        myPwd = ActivityHelper.getMyAccount(this).optString("pwd");
        loadingView = findViewById(R.id.loadingLayoutInclude);
        inputBdNum = (EditText) findViewById(R.id.inputBdNum);
        inputChargeCode = (EditText) findViewById(R.id.inputBdPwd);
        resultTip = (TextView) findViewById(R.id.resultTip);
        chargeReqClient = WebServiceHelper.createChargeRequestClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... position) {
                String supplyID = json.optJSONObject("data").optString("SupplyID");
                confirmChargeClient.callWs(myAccount,myPwd,supplyID);
            }
        });
        confirmChargeClient = WebServiceHelper.createConfirmChargeClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... position) {
                Toast.makeText(ChargeActivity.this,json.optJSONObject("data").optString("SupplyContent"),Toast.LENGTH_SHORT).show();
                String r = json.optJSONObject("data").optString("SupplyContent");
                resultTip.setText(r);
                loadingView.setVisibility(View.GONE);
            }
        });
    }


    public void toCharge(View v){
        loadingView.setVisibility(View.VISIBLE);
        if(inputBdNum.getText() == null || inputBdNum.getText().toString().equals("")){
            Toast.makeText(this,"请输入北斗号",Toast.LENGTH_SHORT).show();

        }else
        if(inputChargeCode.getText() == null || inputChargeCode.getText().toString().equals("")){
            Toast.makeText(this,"请输充值码",Toast.LENGTH_SHORT).show();
        }else {
            chargeReqClient.callWs(myAccount,myPwd,inputBdNum.getText().toString(),inputChargeCode.getText().toString());
        }
    }



}
