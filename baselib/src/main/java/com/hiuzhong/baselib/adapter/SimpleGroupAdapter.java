package com.hiuzhong.baselib.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;


import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiuzhong.baselib.util.ImageLoaderUtil;

public class SimpleGroupAdapter extends BaseExpandableListAdapter {
    public ArrayList<Character> heads;
    public Map<Character, List<JSONObject>> datas;
    public Context cnt;
    private int headLayoutId;
    private int itemLayoutId;
    private int[] itemIds;
    private String[] fieldNames;

    public SimpleGroupAdapter(Context cnt, int headLayoutId, int itemLayoutId, int[] itemIds, String[] fieldNames) {
        this.headLayoutId = headLayoutId;
        this.itemLayoutId = itemLayoutId;
        this.itemIds = itemIds;
        this.fieldNames = fieldNames;
        this.cnt = cnt;
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
        TextView view = (TextView) LayoutInflater.from(cnt).inflate(headLayoutId, null);
        view.setText(heads.get(groupPosition).toString());
        view.setClickable(true);
        return view;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = LayoutInflater.from(cnt).inflate(itemLayoutId, null);
        }
        JSONObject itemData = datas.get(heads.get(groupPosition)).get(childPosition);
        for (int i = 0; i < itemIds.length; i++) {
            fillViewValue(view.findViewById(itemIds[i]), itemData.optString(fieldNames[i]));
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
            try {
                ((ImageView) v).setImageBitmap(ImageLoaderUtil.loadFromFile(cnt, value));
            } catch (Throwable e) {
                Log.e("fillAdapter", e.getLocalizedMessage());
            }
        }
    }


}
