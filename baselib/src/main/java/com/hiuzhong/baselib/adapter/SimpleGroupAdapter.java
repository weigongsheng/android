package com.hiuzhong.baselib.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hiuzhong.baselib.util.ImageLoaderUtil;

public class SimpleGroupAdapter extends BaseExpandableListAdapter {
    private final int headTextId;
    private final String headField;
    public ArrayList<String> heads;
    public Map<String, List<Map<String,String>>> datas;
    public Context cnt;
    private int headLayoutId;
    private int itemLayoutId;
    private int[] itemIds;
    private String[] fieldNames;
    public ViewGroup vg;
    private List<Bitmap> imgs = new ArrayList<>();



    /**
     * 构造函数
     * @param cnt Context
     * @param headLayoutId 分组头部布局资源文件 R.layout.
     * @param itemLayoutId
     * @param itemIds 子项中各个view的id
     * @param fieldNames 各个子view对应 map中key的名称
     */
    public SimpleGroupAdapter(Context cnt,
                              int headLayoutId,
                              int itemLayoutId,
                              int headTextId,
                              String headField,
                              int[] itemIds,
                              String[] fieldNames ) {
        this.headLayoutId = headLayoutId;
        this.itemLayoutId = itemLayoutId;
        this.itemIds = itemIds;
        this.fieldNames = fieldNames;
        this.cnt = cnt;
        this.headTextId = headTextId;
        this.headField = headField;
    }

    public void build(){
        Set<String> allHead = datas.keySet();
        heads = new ArrayList<>(allHead.size());
        heads.addAll(allHead);
        Collections.sort(heads);
    }

    @Override
    public int getGroupCount() {
        return heads.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return datas.get(heads.get(groupPosition)).size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return heads.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return datas.get(heads.get(groupPosition)).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {

        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {

        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        View v = LayoutInflater.from(cnt).inflate(headLayoutId,vg,false);
        TextView view = (TextView)v.findViewById(headTextId);
        view.setText(heads.get(groupPosition));
        view.setClickable(true);
        return v;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(cnt).inflate(itemLayoutId, vg,false);
        }
        Map<String, String> itemData = datas.get(heads.get(groupPosition)).get(childPosition);
        for (int i = 0; i < itemIds.length; i++) {
            fillViewValue(view.findViewById(itemIds[i]), itemData.get(fieldNames[i]));
        }
        return view;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {

        return true;
    }

    private void fillViewValue(View v, String value) {
        if (v instanceof TextView) {
            ((TextView) v).setText(value);
        } else if (v instanceof ImageView) {
            Bitmap bitmap =null;
            try {
                bitmap =ImageLoaderUtil.loadFromFile(cnt, value);
            } catch (Throwable e) {
            }
            if(bitmap != null){
                ((ImageView) v).setImageBitmap(bitmap);
                imgs.add(bitmap);
            }
        }
    }


    public void clear() {
        try {
            heads.clear();
            datas.clear();
            for (Bitmap img:imgs){
                img.recycle();
            }
            imgs.clear();
            notifyDataSetChanged();
        } catch (Exception e) {

        }
    }

}
