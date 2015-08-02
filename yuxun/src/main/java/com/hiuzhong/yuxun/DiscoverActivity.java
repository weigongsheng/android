package com.hiuzhong.yuxun;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;

import com.hiuzhong.baselib.adapter.SimpleGroupAdapter;
import com.hiuzhong.baselib.listener.OnRefreshListener;
import com.hiuzhong.baselib.listener.RefreshResultNotify;
import com.hiuzhong.baselib.view.PullToRefreshLayout;
import com.hiuzhong.yuxun.activity.YuXunActivity;
import com.hiuzhong.yuxun.adapter.SimplePosiGroupAdapter;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;
import com.hiuzhong.yuxun.vo.Contact;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;


public class DiscoverActivity extends YuXunActivity implements OnRefreshListener {

    protected ExpandableListView contactListView;
    protected SimpleGroupAdapter adapter;
    private ContactsDbManager manager;
    PullToRefreshLayout refreshLayout;
    protected WebServiceHelper positionQueryClient;
    private ArrayList<Contact> allBdContact;
    private JSONObject myAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_discover);
        super.onCreate(savedInstanceState);
        contactListView = (ExpandableListView) findViewById(R.id.contactListView);
        refreshLayout = (PullToRefreshLayout) findViewById(R.id.contactListViewContainer);
        refreshLayout.setOnRefreshListener(this);
        manager = new ContactsDbManager(this);
        allBdContact = new ArrayList<Contact>();
        myAccount = ActivityHelper.getMyAccount(this);
        adapter = new SimplePosiGroupAdapter(this,R.layout.layout_contact_head
                ,R.layout.layout_contact_item
                ,R.id.contactHead
                ,"firstChar"
                ,new int[]{ R.id.imageView,R.id.account,R.id.contactNickName,R.id.textPosition,R.id.textTime}
                ,new String[]{"faceImgPath","account","nickName","position","time"});
        adapter.vg=contactListView;
        initListData();
        positionQueryClient = WebServiceHelper.createGetPosiClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json,int ... p) {
                JSONArray ja = json.optJSONObject("data").optJSONArray("BDPostion") ;
                if(ja.length()<1){
                    return;
                }
                JSONObject jp = json.optJSONObject("data").optJSONArray("BDPostion").optJSONObject(0);
                String position = " 经度:"+jp.optString("Lon")+" 纬度:"+jp.optString("Lat");
                String time = jp.optString("ReportTime");
                Map<String,String> data = (Map<String,String>) adapter.getChild(p[0],p[1]);
                data.put("position",position);
                data.put("time",time);
                adapter.notifyDataSetChanged();
            }
        });
        queryPostion();
    }



    private void initListData() {
        List<Contact> allContact = manager.query();
        HashMap<String,List<Map<String,String>>> datas = new HashMap<>();
        for (Contact c:allContact){
            if(!c.isBdAccount()){
                continue;
            }
            allBdContact.add(c);
            List<Map<String,String>> item = datas.get(c.firstChar);
            if(item == null){
                item = new ArrayList<>();
                datas.put(c.firstChar,item);
            }
            Map<String,String> data = new Hashtable<>();
            data.put("faceImgPath",c.faceImgPath);
            data.put("account",c.account);
            data.put("nickName",c.nickName);
            data.put("firstChar", c.firstChar);
            data.put("contactId", String.valueOf(c.id));
            data.put("position", "");
            data.put("time", "");
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

    public void queryPostion(){
            for(int i=0,j = adapter.getGroupCount();i<j;i++){
                for (int k = 0,m= adapter.getChildrenCount(i); k <m ; k++) {
                    Map<String,String> data = (Map<String,String>) adapter.getChild(i,k);
                    positionQueryClient.callWs(i,k,myAccount.optString("account"),myAccount.optString("pwd"), data.get("account"));
                }
            }
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
        queryPostion();
    }

    public void goToChat(int p,int c){
        Map<String,String> data = (Map<String, String>) adapter.getChild(p,c);
        Intent intent = new Intent(this,ContactDetailActivity.class);
        intent.putExtra("contactId", data.get("contactId"));
        startActivity(intent);
    }



}
