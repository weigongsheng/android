package com.hiuzhong.baselib.util;

import android.os.Environment;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class ToolsUtil {
    /**
     * 检查是否存在SDCard
     * @return
     */
    public static boolean hasSdcard(){
        String state = Environment.getExternalStorageState();
        if(state.equals(Environment.MEDIA_MOUNTED)){
            return true;
        }else{
            return false;
        }
    }
}
