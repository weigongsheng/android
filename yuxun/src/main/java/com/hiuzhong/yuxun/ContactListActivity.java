package com.hiuzhong.yuxun;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.hiuzhong.baselib.adapter.SimpleGroupAdapter;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.application.YuXunApplication;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.vo.Contact;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class ContactListActivity extends YuXunActivity {

    protected ExpandableListView contactListView;
    protected SimpleGroupAdapter adapter;


    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_contact_list);
        super.onCreate(savedInstanceState);
        contactListView = (ExpandableListView) findViewById(R.id.contactListView);
        initListData();
    }

    private void initListData() {
        ContactsDbManager manager = new ContactsDbManager(this);
        List<Contact> allContact = manager.query();
        adapter = new SimpleGroupAdapter(this,R.layout.layout_contact_head,R.layout.layout_contact_item,
                new int[]{ R.id.imageView,R.id.account,R.id.contactNickName}
                ,new String[]{"faceImgPath","account","nickName"});
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
            item.add(data);
        }
        adapter.datas=datas;
        adapter.heads = new ArrayList<>(adapter.datas.keySet().size());
        adapter.heads.addAll(adapter.datas.keySet());
        Collections.sort(adapter.heads);
        contactListView.setAdapter(adapter);
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
}
