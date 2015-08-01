package com.hiuzhong.yuxun;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.helper.ActivityHelper;


public class MeActivity extends YuXunActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_me);
        super.onCreate(savedInstanceState);
        ActivityHelper.setTitle(this,getTitle().toString());
    }


}
