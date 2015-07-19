package com.hiuzhong.yuxun;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.dao.MessageDbManager;
import com.hiuzhong.yuxun.vo.Contact;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ChatListActivity extends YuXunActivity {
    private List<Map<String,Object>> datas;
    private SimpleAdapter adapter;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_chat_list);
        super.onCreate(savedInstanceState);
        listView = (ListView) findViewById(R.id.msgListView);
        initData();
    }

    private void initData() {
        datas = new ArrayList<>();
        MessageDbManager msgDao = new MessageDbManager(this);
        ContactsDbManager cantDao = new ContactsDbManager(this);
        List<String[]> lastMsgs = msgDao.queryLastMsg();
        for (String[] info : lastMsgs) {
            String[] msg = msgDao.queryMsgById(info[0]);
            Contact contact = cantDao.queryById(info[1]);
            contact.lastMsg = msg[1];
            Map<String,Object> data = new Hashtable<>();
            Bitmap headImg = ImageLoaderUtil.loadFromFile(this, contact.faceImgPath);
            if(headImg == null ){
                data.put("faceImg", BitmapFactory.decodeResource(getResources(),R.drawable.ic_me));
            }
            data.put("id",""+contact.id);
            data.put("account",contact.account);
            data.put("nickName",contact.nickName);
            data.put("lastMsg",contact.lastMsg.length()>25?contact.lastMsg.substring(0,24)+"...":contact.lastMsg);
            data.put("lastMsgTime",msg[4].substring(5,16));
            datas.add(data);
        }
        adapter = new SimpleAdapter(this,datas,R.layout.layout_msg_list,
                new String[]{ "nickName","lastMsgTime","lastMsg"},
                new int[]{ R.id.list_nicklName,R.id.list_lastMsgTime,R.id.list_lastMsg}){
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                ((ImageView)view.findViewById(R.id.list_contactHeadImg))
                        .setImageBitmap((Bitmap) ((Map<String, Object>) getItem(position)).get("faceImg"));
                return view;
            }
        };
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                toChat((String) datas.get(position).get("id"), position);
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

    public void toChat(String contId,int p){
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("contactId", contId);
        intent.putExtra("p", p);
        startActivityForResult(intent,1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode ==1 && data.getStringExtra("lastMsg") != null ){
            ((TextView) listView.getChildAt(data.getIntExtra("p",-1)).findViewById(R.id.list_lastMsg)).setText(data.getStringExtra("lastMsg"));
        }else{
             super.onActivityResult(requestCode, resultCode, data);
        }
    }


    @Override
    protected void onStart() {
        if(YuXunActivity.LAST_MSG_CHANGED){
            refresh();
            YuXunActivity.LAST_MSG_CHANGED =false;
        }
        super.onStart();
    }

    public void refresh(){
        for (Map<String,Object> da : datas){
            ((Bitmap)da.get("faceImg")).recycle();
            da.clear();
        }
        datas.clear();
//        adapter.notifyDataSetChanged();
        initData();
    }

}
