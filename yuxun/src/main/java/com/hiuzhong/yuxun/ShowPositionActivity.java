package com.hiuzhong.yuxun;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ShowPositionActivity extends Activity {

    private MapView mMapView;
    private BaiduMap mBaiduMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    SDKInitializer.initialize(getApplicationContext());
        setContentView(R.layout.activity_show_position);
    //获取地图控件引用
    mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
//        mMapView.get

//普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        initMap();
    }
//{"code":0,"data":{"BDPostion":[{"BdNum":299305,"Lon":121.51786,"Lat":31.092861,"Speed":0.4,"Direction":0,"ReportTime":"2015-07-29 16:17:08"}
// ,{"BdNum":299305,"Lon":121.51786,"Lat":31.092861,"Speed":0.4,"Direction":0,"ReportTime":"2015-07-29 16:17:08"}
// ,{"BdNum":299305,"Lon":121.51783,"Lat":31.091944,"Speed":1.3,"Direction":350,"ReportTime":"2015-07-29 16:02:03"}
// ,{"BdNum":299305,"Lon":121.517281,"Lat":31.092028,"Speed":0,"Direction":0,"ReportTime":"2015-07-29 15:46:58"}
// ,{"BdNum":299305,"Lon":121.517303,"Lat":31.092611,"Speed":0,"Direction":0,"ReportTime":"2015-07-29 15:31:54"}
// ,{"BdNum":299305,"Lon":121.517387,"Lat":31.092251,"Speed":0.2,"Direction":0,"ReportTime":"2015-07-29 15:16:51"}
// ,{"BdNum":299305,"Lon":121.517693,"Lat":31.092417,"Speed":0,"Direction":0,"ReportTime":"2015-07-29 15:01:46"}
// ,{"BdNum":299305,"Lon":121.51828,"Lat":31.092028,"Speed":0,"Direction":0,"ReportTime":"2015-07-29 14:46:41"}
// ,{"BdNum":299305,"Lon":121.518333,"Lat":31.091888,"Speed":0,"Direction":0,"ReportTime":"2015-07-29 14:31:36"}
// ,{"BdNum":299305,"Lon":121.520386,"Lat":31.088444,"Speed":0,"Direction":0,"ReportTime":"2015-07-29 14:16:31"}]},"tip":"取得北斗位置数据成功"}
    private void initMap() {//121.501991,31.24487

        try {//getIntent().getStringExtra("positionInfo")
            String info ="[{\"BdNum\":299305,\"Lon\":121.51786,\"Lat\":31.092861,\"Speed\":0.4,\"Direction\":0,\"ReportTime\":\"2015-07-29 16:17:08\"},{\"BdNum\":299305,\"Lon\":121.51786,\"Lat\":31.092861,\"Speed\":0.4,\"Direction\":0,\"ReportTime\":\"2015-07-29 16:17:08\"},{\"BdNum\":299305,\"Lon\":121.51783,\"Lat\":31.091944,\"Speed\":1.3,\"Direction\":350,\"ReportTime\":\"2015-07-29 16:02:03\"},{\"BdNum\":299305,\"Lon\":121.517281,\"Lat\":31.092028,\"Speed\":0,\"Direction\":0,\"ReportTime\":\"2015-07-29 15:46:58\"},{\"BdNum\":299305,\"Lon\":121.517303,\"Lat\":31.092611,\"Speed\":0,\"Direction\":0,\"ReportTime\":\"2015-07-29 15:31:54\"},{\"BdNum\":299305,\"Lon\":121.517387,\"Lat\":31.092251,\"Speed\":0.2,\"Direction\":0,\"ReportTime\":\"2015-07-29 15:16:51\"},{\"BdNum\":299305,\"Lon\":121.517693,\"Lat\":31.092417,\"Speed\":0,\"Direction\":0,\"ReportTime\":\"2015-07-29 15:01:46\"},{\"BdNum\":299305,\"Lon\":121.51828,\"Lat\":31.092028,\"Speed\":0,\"Direction\":0,\"ReportTime\":\"2015-07-29 14:46:41\"},{\"BdNum\":299305,\"Lon\":121.518333,\"Lat\":31.091888,\"Speed\":0,\"Direction\":0,\"ReportTime\":\"2015-07-29 14:31:36\"},{\"BdNum\":299305,\"Lon\":121.520386,\"Lat\":31.088444,\"Speed\":0,\"Direction\":0,\"ReportTime\":\"2015-07-29 14:16:31\"}]";
            JSONArray ja = new JSONArray(info);
            LatLng[] position = new LatLng[ja.length()];
            List<LatLng> positions = new ArrayList<>();
            for (int i = 0; i < position.length; i++) {
                JSONObject jp = ja.optJSONObject(i);
                position[position.length-i-1] = new LatLng(Double.parseDouble(jp.optString("Lat")),Double.parseDouble(jp.optString("Lon")));
            }
            for (int i = 0; i <position.length ; i++) {
                positions.add(position[i]);
            }
            OverlayOptions ooPolyline = new PolylineOptions().width(10)
                    .color(0xAAFF0000).points(positions);
        } catch (JSONException e) {
            e.printStackTrace();
        }
//定义Maker坐标点
        LatLng point = new LatLng(31.24487,121.501991);
//构建Marker图标
        BitmapDescriptor bitmap = BitmapDescriptorFactory
                .fromResource(R.drawable.icon_focus_marka);
//构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions()
                .position(point)
                .icon(bitmap);
//在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newLatLng(point));
//        TextView popupText = new TextView(this);
//        popupText.setText("123232");
//        mBaiduMap.showInfoWindow(new InfoWindow(popupText, point, 0));




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
}
