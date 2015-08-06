package com.hiuzhong.yuxun;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;

import org.json.JSONException;
import org.json.JSONObject;


public class ChargeActivity extends Activity {
    public static final int SEND_CHARGE_MSG =0XCDC;
    protected WebServiceHelper chargeReqClient;
    protected WebServiceHelper confirmChargeClient;
    protected String myAccount;
    protected String myPwd;
    protected View loadingView;
    EditText inputBdNum;
    EditText inputChargeCode;
    private TextView resultTip;
    Handler handler;
    Button submitButton;
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
        submitButton = (Button) findViewById(R.id.submitCharge);
        handler = new Handler(){
            @Override
            public void handleMessage(Message msg) {
                if(msg.what ==SEND_CHARGE_MSG){
                     confirmChargeClient.callWs(myAccount,myPwd,msg.getData().getString("suppliId"));
                }
            }
        };
        chargeReqClient = WebServiceHelper.createChargeRequestClient(this, new WsCallBack() {
            @Override
            public void whenError() {
                loadingView.setVisibility(View.GONE);
                resultTip.setText("服务端错误,稍后请重试");
                submitButton.setText("重试");
            }

            @Override
            public void whenFail(JSONObject json) {
                loadingView.setVisibility(View.GONE);
                resultTip.setText(json.optString("tip"));
                submitButton.setText("重试");
            }

            @Override
            public void whenResponse(JSONObject json, Object... position) {
                String supplyID = json.optJSONObject("data").optString("SupplyID");
                if(supplyID == null){
                    resultTip.setText(json.optString("tip"));
                }
                Message msg = new Message();
                msg.what=SEND_CHARGE_MSG;
                Bundle da = new Bundle();
                da.putString("suppliId", json.optJSONObject("data").optString("SupplyID"));
                msg.setData(da);
                handler.sendMessageDelayed(msg, 4 * 1000);
            }
        });
        confirmChargeClient = WebServiceHelper.createConfirmChargeClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... position) {
//                try {
//                    json = new JSONObject("{\"code\":0,\"data\":{\"SupplyContent\":\"按条计费卡充值成功,365天内使用有效\"},\"tip\":\"取得充值结果成功\"}");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
                loadingView.setVisibility(View.GONE);
                resultTip.setText(json.optJSONObject("data").optString("SupplyContent"));
                submitButton.setText("重试");
            }

            @Override
            public void whenError() {
                loadingView.setVisibility(View.GONE);
                resultTip.setText("服务端错误,稍后请重试");
                submitButton.setText("重试");
            }

            @Override
            public void whenFail(JSONObject json) {
                loadingView.setVisibility(View.GONE);
                resultTip.setText(json.optString("tip"));
                submitButton.setText("重试");
            }
        });


    }


    public void toCharge(View v){
        resultTip.setText("");
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
