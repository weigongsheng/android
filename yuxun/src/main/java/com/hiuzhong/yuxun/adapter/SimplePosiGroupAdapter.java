package com.hiuzhong.yuxun.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.hiuzhong.baselib.adapter.SimpleGroupAdapter;
import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.yuxun.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimplePosiGroupAdapter extends SimpleGroupAdapter {


    /**
     * 构造函数
     *
     * @param cnt          Context
     * @param headLayoutId 分组头部布局资源文件 R.layout.
     * @param itemLayoutId
     * @param headTextId
     * @param headField
     * @param itemIds      子项中各个view的id
     * @param fieldNames   各个子view对应 map中key的名称
     */
    public SimplePosiGroupAdapter(Context cnt, int headLayoutId, int itemLayoutId, int headTextId, String headField, int[] itemIds, String[] fieldNames) {
        super(cnt, headLayoutId, itemLayoutId, headTextId, headField, itemIds, fieldNames);
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        View view =  super.getChildView(groupPosition,childPosition,isLastChild,convertView,parent);
        view.findViewById(R.id.textPosition).setVisibility(View.VISIBLE);
        view.findViewById(R.id.textTime).setVisibility(View.VISIBLE);
        return view;
    }








}
