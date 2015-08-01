package com.hiuzhong.yuxun.activity;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.yuxun.ChatListActivity;
import com.hiuzhong.yuxun.ContactListActivity;
import com.hiuzhong.yuxun.DiscoverActivity;
import com.hiuzhong.yuxun.MeActivity;
import com.hiuzhong.yuxun.R;
import com.hiuzhong.yuxun.application.YuXunApplication;

public class YuXunActivity extends Activity {

    public static boolean LAST_MSG_CHANGED=false;
    public static boolean LAST_CONTACT_INFO_CHANGED=false;


    private long lastBackTime;
    public static final int MAX_EXIT_SPAN = 1000;
    public static Class[] activityClass = {ChatListActivity.class, ContactListActivity.class, DiscoverActivity.class, MeActivity.class};
    public static int[] picRes = {R.drawable.ic_chat, R.drawable.ic_contact, R.drawable.ic_discover, R.drawable.ic_me};
    public static int[] curPicRes = {R.drawable.ic_chat_cur, R.drawable.ic_contact_cur, R.drawable.ic_discover_cur, R.drawable.ic_me_cur};
    protected LinearLayout bottonLayout;
    protected TextView titleView;

    public void jumpTo(View view) {
        int index = Integer.parseInt(view.getTag().toString());
        Intent intent = new Intent(this, activityClass[index]);
        intent.putExtra("b_index", index);
        intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT); //FLAG_ACTIVITY_REORDER_TO_FRONT
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        long cur = System.currentTimeMillis();
        if (cur - lastBackTime > MAX_EXIT_SPAN) {
            Toast.makeText(this, "再按一次推出系统", Toast.LENGTH_SHORT).show();
            lastBackTime = cur;
        } else if (lastBackTime > 0) {
            getApplication().onTerminate();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ((YuXunApplication) getApplication()).addActivity(this);
        super.onCreate(savedInstanceState);
        bottonLayout = (LinearLayout) findViewById(R.id.layout_act_bottom);
        if (bottonLayout != null) {
            int i = getIntent().getIntExtra("b_index", -1);
            if (i >= 0) {
                ((ImageView) bottonLayout.getChildAt(i)).setImageResource(curPicRes[i]);
            }
        }
        titleView = (TextView) findViewById(R.id.head_title_text);
        if (titleView != null) {
            titleView.setText(getTitle());
        }
    }

//	@Override
//	protected void onStart() {
//		super.onStart();
//	}

}
