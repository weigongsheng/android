package com.hiuzhong.yuxun.application;

import android.app.Activity;
import android.app.Application;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by gongsheng on 2015/7/8.
 */
public class YuXunApplication extends Application {

    private Set<Activity> activities = new HashSet<Activity>();

    public void addActivity(Activity activity) {
        activities.add(activity);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        for (Activity activity : activities) {
            activity.finish();
        }

        System.exit(0);
    }
}
