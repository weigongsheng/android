package com.hiuzhong.baselib.util;

import java.io.File;

import org.json.JSONException;

import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;

import android.app.AlertDialog.Builder;

import android.app.DownloadManager;

import android.content.BroadcastReceiver;

import android.content.Context;

import android.content.DialogInterface;

import android.content.DialogInterface.OnClickListener;

import android.content.Intent;

import android.content.IntentFilter;

import android.content.pm.PackageInfo;

import android.content.pm.PackageManager;

import android.content.pm.PackageManager.NameNotFoundException;

import android.net.Uri;

import android.os.AsyncTask;

import android.os.Environment;

import android.os.Handler;

import android.os.Message;

import android.widget.Toast;



//

//利用系统自带DownloadManager（2.3以上版本）获取后台版本信息更新下载最新APK资源

// @create_date 2012-07-19

public class UpdateVersionByDownloadManager {

    public static final String TAG = "UpdateVersionByDownloadManager";

    private final static int UPDATE_NOTIFY = 0;

    private final static int DOWN_ERROR = 1;

    private final static int GET_UNDATAINFO_ERROR = 2;

    private final static int INSTALL_ERROR = 3;
    private final String serverVersion;
    private final String downLoadUrl;

    private AndroidVersionBean androidVersionBean = new AndroidVersionBean();

    private String resultStr = "";

    private String versionName = "";

    private String versionCode = "";

    private String url = "";

    private File file;

    private DownloadManager downloadManager;



    private Activity mContext;

    //构造函数

    public UpdateVersionByDownloadManager(Activity context,String serverVersion,String downLoadUrl) {

        this.mContext = context;

        // 注册

        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver();

        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

//        DownloadTask downloadTask = new DownloadTask();
//
//        downloadTask.execute(getRequestContent());
        this.serverVersion = serverVersion;
        this.downLoadUrl = downLoadUrl;



    }

    //调用自带的DownloadManager下载最新的APK

    public void DownloadManagerTool() {

        downloadManager = (DownloadManager)mContext.getSystemService(mContext.DOWNLOAD_SERVICE);

        // 创建下载请求

        DownloadManager.Request down = new DownloadManager.Request(

                Uri.parse(androidVersionBean.getUrl()));

        // 设置允许使用的网络类型，这里是移动网络和wifi都可以

        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);

        // 发出通知，既后台下载

        down.setShowRunningNotification(true);

        // 显示下载界面

        down.setVisibleInDownloadsUi(true);

        down.setTitle("在线设计PAD");

        // down.

        // 设置下载后文件存放的位置

        down.setDestinationInExternalPublicDir("osspad","OnlineDesignForPAD.apk");

        file = new File(Environment.getExternalStoragePublicDirectory("osspad"),"OnlineDesignForPAD.apk");

        // 将下载请求放入队列

        downloadManager.enqueue(down);

    }

//    // 处理器
//
//    Handler handler = new Handler() {
//
//        @Override
//
//        public void handleMessage(Message msg) {
//
//            // TODO Auto-generated method stub
//
//            super.handleMessage(msg);
//
//            switch (msg.what) {
//
//                case UPDATE_NOTIFY:
//
//                    // 对话框通知用户升级程序
//
//                    showUpdataDialog();
//
//                    break;
//
//                case GET_UNDATAINFO_ERROR:
//
//                    // 服务器超时
//
//                    Toast.makeText(mContext, "获取服务器更新信息失败", Toast.LENGTH_SHORT).show();
//
//                    LoginMain();
//
//                    break;
//
//                case DOWN_ERROR:
//
//                    // 下载apk失败
//
//                    Toast.makeText(mContext, "下载新版本失败", Toast.LENGTH_SHORT).show();
//
//                    LoginMain();
//
//                case INSTALL_ERROR:
//
//                    Toast.makeText(mContext, "安装新版本失败",Toast.LENGTH_SHORT).show();
//
//                    LoginMain();
//
//                    break;
//
//            }
//
//        }
//
//    };

    //获取当前APK的版本号

    private String getVersionCode() {

        // 获取packagemanager的实例

        PackageManager packageManager = mContext.getPackageManager();

        // getPackageName()是你当前类的包名，0代表是获取版本信息

        PackageInfo packInfo = new PackageInfo();

        try {

            packInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);

        } catch (NameNotFoundException e) {

            // TODO Auto-generated catch block

            e.printStackTrace();

        }

        return String.valueOf(packInfo.versionCode);

    }

    // 弹出对话框通知用户更新程序 弹出对话框的步骤： 1.创建alertDialog的builder. 2.要给builder设置属性,

    //对话框的内容,样式,按钮 3.通过builder 创建一个对话框 4.对话框show()出来

    protected void showUpdataDialog() {

        Builder builer = new Builder(mContext);

        builer.setTitle(androidVersionBean.getVersionName() + "版本升级");

        builer.setMessage("发现" + androidVersionBean.getVersionName()

                + "最新版,建议立即更新使用");

        // 当点确定按钮时从服务器上下载 新的apk 然后安装

        builer.setPositiveButton("确定", new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                DownloadManagerTool();

            }

        });

        // 当点取消按钮时进行登录

        builer.setNegativeButton("取消",null);

        AlertDialog dialog = builer.create();

        dialog.show();

    }

    public void start(){
        String versionCodeNow = getVersionCode();
        if (versionCodeNow.equals(serverVersion)) {

                    Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
//                    LoginMain();

                } else if(versionCodeNow.compareTo(serverVersion)<0) {
//                    Message msg = new Message();
//                    msg.what = UPDATE_NOTIFY;
//                    handler.sendMessage(msg);
                    showUpdataDialog();

                }
    }


//    class DownloadTask extends AsyncTask<String, Integer, String> {
//
//        @Override
//
//        protected String doInBackground(String... params) {
//
//            String response = "";
//
//            response = getResponse(params);
//
//            return response;
//
//        }
//
//
//
//        @Override
//
//        protected void onPostExecute(String result) {
//
//            // doInBackground返回时触发，换句话说，就是doInBackground执行完后触发
//
//            // 这里的result就是上面doInBackground执行后的返回值
//
//            // 从服务器获取JSON解析并进行比对版本号
//
//            JSONObject holder;
//
//            try {
//
//                holder = new JSONObject(result);
//
//                resultStr = holder.getString("result");
//
//                versionCode = holder.getString("versionCode");
//
//                versionName = holder.getString("versionName");
//
//                url = holder.getString("url");
//
//                androidVersionBean.setVersionCode(versionCode);
//
//                androidVersionBean.setUrl(url);
//
//                androidVersionBean.setVersionName(versionName);
//
//                String versionCodeNow = getVersionCode();
//
//                if (androidVersionBean.getVersionCode().equals(versionCodeNow)) {
//
//                    Toast.makeText(mContext, "已经是最新版本", Toast.LENGTH_SHORT).show();
//
//                    LoginMain();
//
//                } else {
//
//                    Message msg = new Message();
//
//                    msg.what = UPDATE_NOTIFY;
//
//                    handler.sendMessage(msg);
//
//                }
//
//            } catch (JSONException e) {
//
//                Message msg = new Message();
//
//                msg.what = GET_UNDATAINFO_ERROR;
//
//                handler.sendMessage(msg);
//
//                e.printStackTrace();
//
//            }
//
//
//
//            super.onPostExecute(result);
//
//        }
//
//    }

    // 安装apk

    protected void installApk(File file) {

        if (file.toString().endsWith(".apk")) {

            Intent intent = new Intent();

            intent.setAction(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.fromFile(file),

                    "application/vnd.android.package-archive");

            this.mContext.startActivity(intent);

        } else {

//            Message msg = new Message();
//
//            msg.what = INSTALL_ERROR;
//
//            handler.sendMessage(msg);

        }

    }

//    // 设置向后台发送的JSON 用于版本信息
//
//    public String getRequestContent() {
//
//        try {
//
//            JSONObject jsonData = new JSONObject();
//
//            jsonData.put("…", "…");//省略参数
//
//            return "";// http://.../radio/design-order!getAndroidVersion?//省略ip地址
//
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//            return "";
//
//        }
//
//    }
//
//    // 发送请求（HttpBase类需要自己写）
//
//    public String getResponse(String... urls) {
//
//
//
//        String result = "";
//
//        try {
//
//            result ="";// HttpBase.postRequest(urls[0], HttpBase.POST, null);
//
//        } catch (Exception e) {
//
//            e.printStackTrace();
//
//        }
//
//        return result;
//
//    }
//
//    // 返回主界面

//    private void LoginMain() {
//
//        Intent intent = new Intent(mContext, Activity.class);
//
//        mContext.startActivity(intent);
//
//    }

    // 接受下载完成后的intent

    class DownloadCompleteReceiver extends BroadcastReceiver {

        @Override

        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {

//                                  long downId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                installApk(file);

            }

        }

    }

}
