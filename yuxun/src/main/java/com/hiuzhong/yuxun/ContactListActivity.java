package com.hiuzhong.yuxun;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.hiuzhong.baselib.adapter.SimpleGroupAdapter;
import com.hiuzhong.baselib.listener.OnRefreshListener;
import com.hiuzhong.baselib.listener.RefreshResultNotify;
import com.hiuzhong.baselib.view.PullToRefreshLayout;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.vo.Contact;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class ContactListActivity extends YuXunActivity implements OnRefreshListener {

    protected ExpandableListView contactListView;
    protected SimpleGroupAdapter adapter;
    private ContactsDbManager manager;
    PullToRefreshLayout refreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contact_list);
        super.onCreate(savedInstanceState);
        contactListView = (ExpandableListView) findViewById(R.id.contactListView);

        refreshLayout = (PullToRefreshLayout) findViewById(R.id.contactListViewContainer);
        refreshLayout.setOnRefreshListener(this);
        manager = new ContactsDbManager(this);
        adapter = new SimpleGroupAdapter(this,R.layout.layout_contact_head
                ,R.layout.layout_contact_item
                ,R.id.contactHead
                ,"firstChar"
                ,new int[]{ R.id.imageView,R.id.account,R.id.contactNickName}
                ,new String[]{"faceImgPath","account","nickName"});
        adapter.vg=contactListView;
        initListData();
    }

    private void initListData() {
        List<Contact> allContact = manager.query();
        HashMap<String,List<Map<String,String>>> datas = new HashMap<>();
        for (Contact c:allContact){
            String fc = c.firstChar ;
            List<Map<String,String>> item = datas.get(c.firstChar);
            if(item == null){
                item = new ArrayList<>();
                datas.put(c.firstChar,item);
            }
            Map<String,String> data = new Hashtable<>();
            data.put("faceImgPath",c.faceImgPath);
            data.put("account",c.account);
            data.put("nickName",c.nickName);
            data.put("firstChar",c.firstChar);
            data.put("contactId",String.valueOf(c.id));
            item.add(data);
        }
        adapter.datas=datas;
        adapter.build();
        contactListView.setAdapter(adapter);
        for (int i = 0, k = contactListView.getCount(); i < k; i++) {
            contactListView.expandGroup(i);
        }
        contactListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                goToChat(groupPosition, childPosition);
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_contact_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_title) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toAddContact(View view){
        Intent intent = new Intent(this,AddContactActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRefresh(RefreshResultNotify notify) {
        adapter.clear();
        initListData();
        notify.refreshSuccess();
    }

    public void goToChat(int p,int c){
        Map<String,String> data = (Map<String, String>) adapter.getChild(p,c);
        Intent intent = new Intent(this,EditContactActivity.class);
        intent.putExtra("contactId", data.get("contactId"));
        startActivity(intent);
    }
}
