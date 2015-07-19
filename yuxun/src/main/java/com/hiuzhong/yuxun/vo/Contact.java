package com.hiuzhong.yuxun.vo;

import android.provider.BaseColumns;
import android.text.StaticLayout;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class Contact {
    public static String MSG_SPLIT ="__";
    public int id;
    public String faceImgPath;
    public String nickName;
    public String account;
    public String firstChar;
    public String lastMsg;

    public String[] getLastMsgInfo(){
        if(lastMsg == null){
            return null;
        }
        String[] temp = lastMsg.split(MSG_SPLIT);
        if(temp.length<3){
            return null;
        }
        String[] re = new String[3];
        re[0] = temp[0];
        re[1] = temp[1];
        re[2] = temp[2];
        if(temp.length >3){
           re[2] = lastMsg.substring(re[0].length()+re[1].length()+MSG_SPLIT.length()*2);
        }

        return re;
    }

}
