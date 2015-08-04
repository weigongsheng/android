package com.hiuzhong.yuxun;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.hiuzhong.yuxun.helper.ActivityHelper;


public class MsgShowActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_msg_show);
        ActivityHelper.initHeadInf(this, getIntent().getStringExtra("title"));
                ((TextView) findViewById(R.id.textContent)).setText(getIntent().getStringExtra("msg"));

        ActivityHelper.initHeadInf(this, getIntent().getStringExtra("title"));
        ((TextView)findViewById(R.id.textContent)).setText(getIntent().getStringExtra("msg"));
    }


}
