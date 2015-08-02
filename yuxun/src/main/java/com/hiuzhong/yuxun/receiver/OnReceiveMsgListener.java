package com.hiuzhong.yuxun.receiver;

import org.json.JSONArray;

/**
 * Created by gongsheng on 2015/8/2.
 */
public interface OnReceiveMsgListener {
    public void onMessage(String account ,String msg ,String time);
}
