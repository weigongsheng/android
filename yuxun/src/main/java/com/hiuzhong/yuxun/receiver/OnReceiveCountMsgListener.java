package com.hiuzhong.yuxun.receiver;

import java.util.Map;

/**
 * Created by gongsheng on 2015/8/2.
 */
public interface OnReceiveCountMsgListener {

    public void onMessage(Map<String,Integer> countInfo);
}

