package com.hiuzhong.yuxun.helper;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.Toast;

import com.hiuzhong.yuxun.MsgShowActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.ksoap2.SoapEnvelope;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapPrimitive;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.security.auth.callback.Callback;

/**
 * Created by gongsheng on 2015/7/25.
 */
public class WebServiceHelper {
    public static final String WS_NS = "http://tempuri.org/";//http://tempuri.org/
    public static final String URL = "http://service.ubinavi.com.cn:3339/Appwebsite/appservice.asmx";

    public static final String SEND_SMS_PWD="ubi12345%%";
    public static final int WS_ERROR = 0XEE1;
    public static final int WS_WSTIP = 0X1F;
    public static final int WS_SUCCESS = 0X5cc;
    private final String methodName;
    private final String resultName;
    private final WsCallBack callBck;

    protected Handler wsHanlder;
    private Context cnt;
    private String[] paraName;

    public WebServiceHelper(Context cnt, String methodName, String resultName, WsCallBack callBack, String... paraName) {
        this.cnt = cnt;
        this.methodName = methodName;
        this.resultName = resultName;
        this.callBck = callBack;
        wsHanlder = createMsgHandler(cnt, callBack);
        this.paraName = paraName;
    }


    public static class AysncWsTask extends AsyncTask<WsPara, Void, Void> {
        private final String methodName;
        private final String resultName;
        private final Handler msgHandler;
        private WsCallBack callBack;

        protected AysncWsTask(Handler msgHandler, String methodName, String resultName, WsCallBack callBack) {
            this.msgHandler = msgHandler;
            this.callBack = callBack;
            this.methodName = methodName;
            this.resultName = resultName;
        }

        protected Void doInBackground(WsPara... params) {
            try {
                WebServiceHelper.callWS(msgHandler, methodName, resultName, callBack, params);
            } catch (Exception e) {
                e.printStackTrace();
                sendErrorMsg(msgHandler, "服务器异常");
            }
            return null;
        }
    }

    protected static void sendErrorMsg(Handler msgHandler, String msg) {
        if (msgHandler == null) {
            return;
        }
        Message m = new Message();
        Bundle b = new Bundle();
        b.putString("tip", msg);
        m.what = WS_ERROR;
        m.setData(b);
        msgHandler.sendMessage(m);
    }

    //    public static void login(Handler hanlder,final String userName, final String pwd, final WsCallBack callBack) throws XmlPullParserException, IOException {
//        new AsyncTask<WsPara,Void,Void>(){
//            @Override
//            protected Void doInBackground(WsPara... params) {
//                try {
//                    WebServiceHelper.callWS(null, "WebLoginAppSys", "WebLoginAppSysResult",callBack,params);
//                } catch (XmlPullParserException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute(new WsPara("username", userName), new WsPara("pwd", pwd));
//    }
    public static WebServiceHelper createLoginClient(Context cnt, WsCallBack callBack) {
        return new WebServiceHelper(cnt, "WebLoginAppSys", "WebLoginAppSysResult", callBack, "username", "pwd");
    }

    /**
     * 注册
     *
     * @param msgHanlder
     * @param userName
     * @param pwd
     * @param mobileNum
     * @param callBack
     */
    public static void regist(Handler msgHanlder, final String userName, final String pwd, String mobileNum, WsCallBack callBack) {
        new AysncWsTask(msgHanlder, "WebRegisterAppUser", "WebRegisterAppUserResult", callBack)
                .execute(new WsPara("username", userName)
                        , new WsPara("pwd", pwd)
                        , new WsPara("mobileNum", mobileNum));
    }
//    public static void queryPwd(Handler msgHanlder, final String userName, WsCallBack callBack) {
//        new AysncWsTask(msgHanlder,"WebQueryPasswd","WebQueryPasswdResult",callBack)
//                .execute(new WsPara("username", userName) );
//    }

    public static WebServiceHelper createQueryPwdClient(Context cnt, WsCallBack callBack) {
        return new WebServiceHelper(cnt, "WebQueryPasswd", "WebQueryPasswdResult", callBack, "username");
    }

    public static WebServiceHelper createChangePwdClient(Context cnt, WsCallBack callBack) {
        return new WebServiceHelper(cnt, "WebChgAppPasswd", "WebChgAppPasswdResult", callBack, "username", "pwd", "newPwd");
    }


//    public static void asyCall(final String method,final String respResult, final WsCallBack callBack,WsPara... params) throws XmlPullParserException, IOException {
//        new AsyncTask<WsPara,Void,Void>(){
//            @Override
//            protected Void doInBackground(WsPara... params) {
//                try {
//                    WebServiceHelper.callWS(activity, method,respResult,callBack,params);
//                } catch (XmlPullParserException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                return null;
//            }
//        }.execute(params);
//    }


    //WebLoginAppSys  WebLoginAppSysResult
    public static final JSONObject callWS(Handler handler, String method, String respResult, WsCallBack callBack, WsPara... para) throws XmlPullParserException, IOException {
        HttpTransportSE ht = new HttpTransportSE(URL);
        ht.debug = true;
        SoapObject request = new SoapObject(WS_NS, method);
        for (int i = 0; i < para.length; i++) {
            request.addProperty(para[i].name, para[i].value);
        }
        SoapSerializationEnvelope envelope = new
                SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        ht.call(WS_NS + method, envelope);

        SoapObject result = (SoapObject) envelope.bodyIn;

        SoapPrimitive detail = (SoapPrimitive) result.getProperty(respResult);
        try {
            if (detail.getValue() == null || detail.getValue().toString().length() < 1) {
                sendErrorMsg(handler, "服务无返回数据");
            }
            JSONObject json = new JSONObject(detail.getValue().toString());
            if (json != null && json.optInt("code", -1) == 0) {
                if (callBack != null) {
                    callBack.whenResponse(json);
                }
                return json;
            } else {
                sendErrorMsg(handler, json.optString("tip", ""));
            }
        } catch (JSONException e) {
            sendErrorMsg(handler, "服务数据错误");
        }
        return null;
    }

    public static final String callWS(String method, String respResult, WsPara... para) throws XmlPullParserException, IOException {
        HttpTransportSE ht = new HttpTransportSE(URL);
        ht.debug = true;
        SoapObject request = new SoapObject(WS_NS, method);
        for (int i = 0; i < para.length; i++) {
            request.addProperty(para[i].name, para[i].value);
        }
        SoapSerializationEnvelope envelope = new
                SoapSerializationEnvelope(SoapEnvelope.VER11);
        envelope.bodyOut = request;
        envelope.dotNet = true;
        envelope.setOutputSoapObject(request);
        ht.call(WS_NS + method, envelope);

        SoapObject result = (SoapObject) envelope.bodyIn;

        SoapPrimitive detail = (SoapPrimitive) result.getProperty(respResult);
        return detail.getValue().toString();
    }

    public static Handler createMsgHandler(final Context act) {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WS_ERROR) {
                    Toast.makeText(act, msg.getData().getString("tip"), Toast.LENGTH_SHORT).show();
                }
                if (msg.what == WS_WSTIP) {
                    Intent intent = new Intent(act, MsgShowActivity.class);
                    intent.putExtra("msg", msg.getData().getString("msg"));
                    intent.putExtra("title", msg.getData().getString("title"));
                    act.startActivity(intent);
                }
            }
        };
    }

    ;

    public static Handler createMsgHandler(final Context act, final WsCallBack callBack) {
        return new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == WS_SUCCESS) {
                    try {
                        callBack.whenResponse(new JSONObject(msg.getData().getString("json")));
                    } catch (JSONException e) {
                    }
                } else if (msg.what == WS_ERROR) {
                    Toast.makeText(act, msg.getData().getString("tip"), Toast.LENGTH_SHORT).show();
                } else if (msg.what == WS_WSTIP) {
                    Intent intent = new Intent(act, MsgShowActivity.class);
                    intent.putExtra("msg", msg.getData().getString("msg"));
                    intent.putExtra("title", msg.getData().getString("title"));
                    act.startActivity(intent);
                }
            }
        };
    }

    ;

    public static JSONObject queryMsg(String account, String pwd) {
        try {
            return WebServiceHelper.callWS(null, "WebGetAppToAppMsg", "WebGetAppToAppMsgResult", null
                    , new WsPara("username", account), new WsPara("pwd", pwd));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    public static JSONObject queryBdMsg(String account, String pwd) {
        try {
            return WebServiceHelper.callWS(null, "WebGetBdToAppMsg", "WebGetBdToAppMsgResult", null
                    , new WsPara("username", account), new WsPara("pwd", pwd));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static WebServiceHelper createShowMsg(Context cnt, WsCallBack callBack) {
        return new WebServiceHelper(cnt, "WebGetCoporationInfo", "WebGetCoporationInfoResult", callBack, "flag");
    }

    public WebServiceHelper callWs(Object... values) {
        WsPara[] para = null;
        final List  extraPara = new ArrayList();
        if(paraName.length>0){
            para = new WsPara[paraName.length];
            int i = 0;
            for (; i < paraName.length; i++) {
                para[i] = new WsPara(paraName[i], values[i].toString());
            }
            if(values.length>paraName.length){//有额外参数
                int length =values.length -paraName.length;
                int start = paraName.length;
                for (int j =0; j < length; j++) {
                    extraPara.add(values[start+j]);
                }
            }
        }

        new AsyncTask<WsPara, Void, String>() {
            @Override
            protected String doInBackground(WsPara... params) {
                try {
                    return WebServiceHelper.callWS(WebServiceHelper.this.methodName, WebServiceHelper.this.resultName, params);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String data) {
                if (data == null) {
                    Toast.makeText(WebServiceHelper.this.cnt, "服务端错误", Toast.LENGTH_SHORT).show();
                    if(WebServiceHelper.this.callBck != null){
                        callBck.whenError();
                    }
                } else {
                    try {
                        JSONObject jsonObject = new JSONObject(data);
                        if (jsonObject.optInt("code", -1) != 0) {
                            Toast.makeText(WebServiceHelper.this.cnt, jsonObject.optString("tip"), Toast.LENGTH_SHORT).show();
                            WebServiceHelper.this.callBck.whenFail(jsonObject);
                        } else {
                            WebServiceHelper.this.callBck.whenResponse(jsonObject,extraPara.size()>0?extraPara.toArray():null);
                        }
                    } catch (JSONException e) {
                        Toast.makeText(WebServiceHelper.this.cnt, "服务端数据错误", Toast.LENGTH_SHORT).show();
                        WebServiceHelper.this.callBck.whenError();
                    }
                }
            }
        }.execute(para);
        return this;
    }

    public static WebServiceHelper createSendAppClient(Context cnt, WsCallBack callBack) {
        return new WebServiceHelper(cnt, "WebSetAppToAppMsg", "WebSetAppToAppMsgResult", callBack, "username", "pwd", "recvAppNum", "content");
    }

    public static WebServiceHelper createSendBdClient(Context cnt, WsCallBack callBack) {
        return new WebServiceHelper(cnt, "WebSetAppToBdMsg", "WebSetAppToBdMsgResult", callBack, "username", "pwd", "bdNum", "content");
    }

    public static WebServiceHelper createQueryBalanceReqClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebQueryBDBalanceRequest","WebQueryBDBalanceRequestResult",
                callBack,"username","pwd","bdNumber");
    }

    public static WebServiceHelper createQueryBalanceClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebQueryBDBalanceResult","WebQueryBDBalanceResultResult",
                callBack,"username","pwd","tabId");
    }

    public static WebServiceHelper createRequestPosiClient(Context cnt,WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebSetAppViewBdPostionRequest","WebSetAppViewBdPostionRequestResult",
                callBack,"username","pwd","bdNum");
    }
    public static WebServiceHelper createGetPosiClient(Context cnt,WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebGetBdPostion","WebGetBdPostionResult",
                callBack,"username","pwd","bdNum");
    }

    public static WebServiceHelper createGrantListClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebGetGrantBdList","WebGetGrantBdListResult",
                callBack,"username","pwd");

    }

    public static WebServiceHelper createGrantCancelClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebDeleteBDGrantAppViewPostion","WebDeleteBDGrantAppViewPostionResult",
                callBack,"username","pwd","bdNum");

    }
    public static WebServiceHelper createGrantRegClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebSetAppViewBdPostionRequest","WebSetAppViewBdPostionRequestResult",
                callBack,"username","pwd","bdNum");

    }
    public static WebServiceHelper createChargeRequestClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebSupplyForBDRequest","WebSupplyForBDRequestResult",
                callBack,"username","pwd","bdNumber","supplyCode");

    }
    public static WebServiceHelper createConfirmChargeClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebSupplyForBDResult","WebSupplyForBDResultResult",
                callBack,"username","pwd","tabId");

    }
    public static WebServiceHelper createSendSMSVCClient(Context cnt, WsCallBack callBack){
        return new WebServiceHelper(cnt,"WebSendToMobileMsg","WebSendToMobileMsgResult",
                callBack,"passwd","mobileNum","content");

    }

    public static String createVcCode(){
        Random r = new Random(System.currentTimeMillis());
        String s = String.valueOf(r.nextDouble());
        return s.substring(2,6);
    }
    public static String sendVc(String phone,Context cnt, final WsCallBack callBack){
        final String vc = createVcCode();
             createSendSMSVCClient(cnt, new WsCallBack() {
                @Override
                public void whenResponse(JSONObject json, Object... extraPara) {
                    if(callBack!= null){
                        callBack.whenResponse(json,vc);
                    }
                }
                @Override
                public void whenError() {
                    if(callBack!= null){
                        callBack.whenError();
                    }
                }

                @Override
                public void whenFail(JSONObject json) {
                    if(callBack!= null){
                        callBack.whenFail(json);
                    }
                }
            }).callWs(SEND_SMS_PWD,phone,"您的此次验证码为:"+vc);
        return vc;
    };


}
