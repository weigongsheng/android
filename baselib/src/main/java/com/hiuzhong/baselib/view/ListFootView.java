package com.hiuzhong.baselib.view;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.baselib.R;
import com.hiuzhong.baselib.listener.ListFootLoadCallback;
import com.hiuzhong.baselib.listener.ListLoadActListener;


public class ListFootView extends LinearLayout implements ListFootLoadCallback {

    public static final int STATUS_FINISH_SUCCESS = 1;
    public static final int STATUS_DEFAULT = 0;
    public static final int STATUS_FINISH_FAIL = 3;
    public static final int STATUS_LOADING = 2;

    public ListLoadActListener loadListener;

    // protected Context context;
    protected TextView tipTextView;
    protected View progressIcoView;

    protected Handler msgHandler;

    public ListFootView(Context context) {
        super(context);
        init();
    }

    public ListFootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ListFootView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public static class FootViewMsgHandler extends Handler {
        private ListFootView view;

        public FootViewMsgHandler(ListFootView view) {
            this.view = view;
        }

        public void handleMessage(Message msg) {
            if (msg.what == LOAD_RESULT_FAIL) {
                Toast.makeText(view.getContext(), "加载失败", Toast.LENGTH_SHORT).show();
            } else if (msg.what == LOAD_RESULT_NO_MORE_DATA) {
                Toast.makeText(view.getContext(), "没有更多内容", Toast.LENGTH_SHORT).show();
            } else if (msg.what == LOAD_RESULT_ABORT) {
                Toast.makeText(view.getContext(), "加载取消", Toast.LENGTH_SHORT).show();
                view.changStatus(STATUS_DEFAULT);
                return;
            }
            view.changStatus(STATUS_FINISH_SUCCESS);
        }
    }



    private void init() {
        msgHandler = new FootViewMsgHandler(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        initFootView();
        this.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    public void initFootView() {
        tipTextView = (TextView) this.findViewById(R.id.listFooterTextTip);
        progressIcoView = this.findViewById(R.id.listFooterProgressBar);
        changStatus(STATUS_DEFAULT);
        tipTextView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                changStatus(STATUS_LOADING);
                if (loadListener != null) {
                    loadListener.startLoad(ListFootView.this);
                }
            }
        });
    }

    @Override
    public void loadFinish(final int status) {
        msgHandler.sendEmptyMessageDelayed(status, 1000);
    }

    protected void changStatus(int state) {
        switch (state) {
            case STATUS_LOADING:
                tipTextView.setText("加载中");
                tipTextView.setVisibility(View.GONE);
                progressIcoView.setVisibility(View.VISIBLE);
                break;
            case STATUS_DEFAULT:
                tipTextView.setText("点击加载更多");
                tipTextView.setVisibility(View.VISIBLE);
                progressIcoView.setVisibility(View.GONE);
                break;
            case STATUS_FINISH_FAIL:
                tipTextView.setText("加载失败");
                tipTextView.setVisibility(View.VISIBLE);
                progressIcoView.setVisibility(View.GONE);
                break;

            default:
                tipTextView.setText("加载完毕");
                tipTextView.setVisibility(View.VISIBLE);
                progressIcoView.setVisibility(View.GONE);
                break;
        }
    }

    public ListFootView setOnLoadStateChangeListener(
            ListLoadActListener listener) {
        this.loadListener = listener;
        return this;
    }

}
