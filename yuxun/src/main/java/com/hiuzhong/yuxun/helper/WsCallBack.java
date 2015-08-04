package com.hiuzhong.yuxun.helper;

import org.json.JSONObject;

/**
 * Created by gongsheng on 2015/7/25.
 */
public abstract class WsCallBack {
        public abstract void whenResponse(JSONObject json,int ... position) ;
        public void whenError(){

        };
        public void whenFail(JSONObject json){

        };
}
