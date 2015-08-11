package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.model.LatLng;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.helper.BaiduGpsConvertHelper;
import com.hiuzhong.yuxun.helper.WebServiceHelper;
import com.hiuzhong.yuxun.helper.WsCallBack;
import com.hiuzhong.yuxun.vo.Contact;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class ShowPositionActivity extends Activity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;
    protected WebServiceHelper positionQueryClient;
    private String nickName;
    private String account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_show_position);
        //获取地图控件引用
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        nickName = getIntent().getStringExtra("nickName");
        account = getIntent().getStringExtra("account");
        ActivityHelper.initHeadInf(this,nickName==null?"定位信息":nickName);
//普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        positionQueryClient = WebServiceHelper.createGetPosiClient(this, new WsCallBack() {
            @Override
            public void whenResponse(JSONObject json, Object... p) {
                JSONArray ja = json.optJSONObject("data").optJSONArray("BDPostion");
                if (ja.length() < 1) {
                    Toast.makeText(ShowPositionActivity.this, "没有位置信息", Toast.LENGTH_SHORT);
                    return;
                }
                addLine(ja);
            }
        });
        initMap();
        refresh();
    }

    private void addLine(JSONArray ja) {
        List<LatLng> positions = new ArrayList<>();
        LatLng[] position = new LatLng[ja.length()];
        for (int i = 0; i < position.length; i++) {
            JSONObject jp = ja.optJSONObject(position.length - i - 1);
            position[i] = BaiduGpsConvertHelper.convertGpsToBaidu(new LatLng(Double.parseDouble(jp.optString("Lat")), Double.parseDouble(jp.optString("Lon"))));
            positions.add(position[i]);
        }
        BitmapDescriptor custom1 = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_landing_arrow);
//        List<BitmapDescriptor>customList = new ArrayList<BitmapDescriptor>();
//        customList.add(custom1);
        OverlayOptions ooPolyline = new PolylineOptions().width(10).points(positions).customTexture(custom1);
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_gcoding);
        OverlayOptions option = new MarkerOptions()
                .position(positions.get(positions.size() - 1))
                .icon(bitmap);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(position[position.length - 1]));
        mBaiduMap.addOverlay(option);
        mBaiduMap.addOverlay(ooPolyline);
        LatLng pt = position[position.length - 1];
        TextView textView = new TextView(this);
        textView.setBackgroundColor(0xffffff);
        textView.setText(ja.optJSONObject(0).optString("ReportTime"));
//创建InfoWindow , 传入 view， 地理坐标， y 轴偏移量
        InfoWindow mInfoWindow = new InfoWindow(textView, pt, -77);
//显示InfoWindow
        mBaiduMap.showInfoWindow(mInfoWindow);
        MapStatusUpdate u = MapStatusUpdateFactory.zoomTo(15);
        mBaiduMap.animateMapStatus(u);
    }

    private void initMap() {
        LatLng point = new LatLng(31.24487, 121.501991);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));

    }

    public void refresh(){
        positionQueryClient.callWs(ActivityHelper.getMyAccount(this).optString("account"),ActivityHelper.getMyAccount(this).optString("pwd"), account);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mMapView.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        //在activity执行onResume时执行mMapView. onResume ()，实现地图生命周期管理
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //在activity执行onPause时执行mMapView. onPause ()，实现地图生命周期管理
        mMapView.onPause();
    }

    public void showContactDetail(View view) {
        Intent intent = new Intent(this,ContactDetailActivity.class);
        Bundle data = new Bundle();
        Contact c = ContactsDbManager.getInstance(this).queryByAccount(account);
        intent.putExtra("contactId", String.valueOf(c.id));
        startActivity(intent);
    }
}
