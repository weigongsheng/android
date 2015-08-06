package com.hiuzhong.yuxun.application;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.dao.MsgCountDbManager;
import com.hiuzhong.yuxun.service.MsgService;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gongsheng on 2015/7/8.
 */
public class YuXunApplication extends Application {

    private Set<Activity> activities = new HashSet<Activity>();
    private Set<Activity> registActivity = new HashSet<Activity>();

    public void addActivity(Activity activity) {
        activities.add(activity);
    }
    public void addRegistAct(Activity activity) {
        registActivity.add(activity);
    }

    public void finishReg(){
        for(Activity a: registActivity){
            a.finish();
        }
        registActivity.clear();
    }

    @Override
    public void onCreate() {
//        System.setProperty("http.proxySet", "true");
//		System.setProperty("http.proxyHost", "172.31.1.246");
//		System.setProperty("http.proxyPort", "8080");
        super.onCreate();
    }

    @Override
    public void onTerminate() {
        closeAllAct();
        super.onTerminate();
        ContactsDbManager.close();
        MessageDbManager.close();
        MsgCountDbManager.close();
        System.exit(0);
    }
        public void closeAllAct(){
        stopService(new Intent(this,MsgService.class));
        finishReg();
        for (Activity activity : activities) {
            activity.finish();
        }
        activities.clear();

    }
}
