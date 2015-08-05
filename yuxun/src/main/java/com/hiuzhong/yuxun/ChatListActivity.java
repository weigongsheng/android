package com.hiuzhong.yuxun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hiuzhong.baselib.listener.OnRefreshListener;
import com.hiuzhong.baselib.listener.RefreshResultNotify;
import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.baselib.view.PullToRefreshLayout;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.constant.Constants;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.dao.MsgCountDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.receiver.OnReceiveCountMsgListener;
import com.hiuzhong.yuxun.service.MsgService;
import com.hiuzhong.yuxun.vo.Contact;


import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class ChatListActivity extends YuXunActivity implements OnRefreshListener {
    private List<Map<String, Object>> datas;
    private SimpleAdapter adapter;
    private ListView listView;
    protected Bitmap defHeadBitmap;
    protected Handler msgHandler;
    private MsgCountDbManager countDbo;
    protected PullToRefreshLayout refreshLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chat_list);
        super.onCreate(savedInstanceState);
        listView = (ListView) findViewById(R.id.msgListView);
        defHeadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_head);
        countDbo =   MsgCountDbManager.getInstance(this);
        refreshLayout = (PullToRefreshLayout) findViewById(R.id.contactListViewContainer);
        refreshLayout.setOnRefreshListener(this);
        initData();
        initMsgListener();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if(ActivityHelper.msgChanged){
            refresh();
            ActivityHelper.msgChanged=false;
        }
    }

    private void initMsgListener() {
        msgHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == Constants.MSG_WHAT_COUNT) {
                    refresh();
                }
            }
        };

        MsgService service = MsgService.getCurService();
        service.setCountMsgListener(new OnReceiveCountMsgListener() {

            @Override
            public void onMessage(Map<String, Integer> countInfo) {
                if (msgHandler != null) {
                    msgHandler.sendEmptyMessage(Constants.MSG_WHAT_COUNT);
                }
            }
        });
    }


    private void initData() {
        datas = new ArrayList<>();
        if(defHeadBitmap.isRecycled()){
            defHeadBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_head);
        }
        MessageDbManager msgDao =   MessageDbManager.getInstance(this);
        ContactsDbManager cantDao =   ContactsDbManager.getInstance(this);
        List<String[]> lastMsgs = msgDao.queryLastMsg();
        for (String[] info : lastMsgs) {
            String[] msg = msgDao.queryMsgById(info[0]);
            Contact contact = cantDao.queryByAccount(info[1]);
            if (contact == null) {

                continue;
            }
            contact.lastMsg = msg[1];
            Map<String, Object> data = new Hashtable<>();
            Bitmap headImg = ImageLoaderUtil.loadFromFile(this, contact.faceImgPath);
            if (headImg == null) {
                headImg = defHeadBitmap;
            }
            Integer count = null;
            data.put("account", contact.account);
            count = countDbo.queryCount(contact.account);
            data.put("faceImg", headImg);
            data.put("id", "" + contact.id);
            data.put("nickName", contact.nickName);
            data.put("newMsgTip", count == null ? 0 : count);
            data.put("lastMsg", contact.lastMsg.length() > 25 ? contact.lastMsg.substring(0, 24) + "..." : contact.lastMsg);
            data.put("lastMsgTime", msg[4].substring(5, 16));
            datas.add(data);
        }
        adapter = new SimpleAdapter(this, datas, R.layout.layout_msg_list,
                new String[]{"nickName", "lastMsgTime", "lastMsg", "newMsgTip"},
                new int[]{R.id.list_nicklName, R.id.list_lastMsgTime, R.id.list_lastMsg, R.id.list_textNumTip}) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((ImageView) view.findViewById(R.id.detailContactHeadImg))
                        .setImageBitmap((Bitmap) ((Map<String, Object>) getItem(position)).get("faceImg"));
                TextView tip = (TextView) view.findViewById(R.id.list_textNumTip);
                String count = tip.getText().toString();
                if (Integer.parseInt(count) > 0) {
                    tip.setVisibility(View.VISIBLE);
                }
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                view.findViewById(R.id.list_textNumTip).setVisibility(View.GONE);
                countDbo.cleanCount((String) datas.get(position).get("account"));
                toChat((String) datas.get(position).get("account"), position);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toChat(String account, int p) {
        Intent intent = new Intent(this, ChatActivity.class);
        intent.putExtra(ChatActivity.INTENT_PARA_CONTACT, account);
        intent.putExtra("p", p);
        startActivityForResult(intent, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && data.getStringExtra("lastMsg") != null) {
            int p = data.getIntExtra("p", -1);
            if(p<0){
                return;
            }
            if(datas!= null && datas.get(p) != null){
                datas.get(p).put("lastMsg",data.getStringExtra("lastMsg"));
                adapter.notifyDataSetChanged();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onStart() {
        if (YuXunActivity.LAST_MSG_CHANGED) {
            refresh();
            YuXunActivity.LAST_MSG_CHANGED = false;
        }
        super.onStart();
    }

    public void refresh() {
        for (Map<String, Object> da : datas) {
            try {
                ((Bitmap) da.get("faceImg")).recycle();
            } catch (Exception e) {
                continue;
            }
            da.clear();
        }
        datas.clear();
//        adapter.notifyDataSetChanged();
        initData();
    }

    @Override
    public void onRefresh(RefreshResultNotify notify) {
        refresh();
        notify.refreshSuccess();
    }
}
