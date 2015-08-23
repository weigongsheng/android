package com.hiuzhong.baselib.util;

import java.io.File;


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


import android.os.Environment;



public class UpdateVersionByDownloadManager {

    public static final String TAG = "UpdateVersionByDownloadManager";

    private final String serverVersion;
    private final String downLoadUrl;

   private UpadateConfirmListenr listenr;

    private File file;

    private DownloadManager downloadManager;



    private Activity mContext;

    //构造函数

    public UpdateVersionByDownloadManager(Activity context,String serverVersion,String downLoadUrl,UpadateConfirmListenr listenr) {

        this.mContext = context;

        // 注册

        DownloadCompleteReceiver receiver = new DownloadCompleteReceiver();

        mContext.getApplication().registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        this.serverVersion = serverVersion;
        this.downLoadUrl = downLoadUrl;
        this.listenr = listenr;



    }


    public void DownloadManagerTool() {

        downloadManager = (DownloadManager)mContext.getSystemService(mContext.DOWNLOAD_SERVICE);
        DownloadManager.Request down = new DownloadManager.Request(
                Uri.parse(downLoadUrl));
        down.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE | DownloadManager.Request.NETWORK_WIFI);
        down.setShowRunningNotification(true);
        down.setVisibleInDownloadsUi(true);
        down.setTitle("普适渔讯宝");
        String fileName ="yuxun"+System.currentTimeMillis()+".apk";
        down.setDestinationInExternalPublicDir("yuxun",fileName);
        file = new File(Environment.getExternalStoragePublicDirectory("yuxun"),fileName);
        downloadManager.enqueue(down);

    }

    private String getVersionCode() {
        PackageManager packageManager = mContext.getPackageManager();
        PackageInfo packInfo = new PackageInfo();
        try {
            packInfo = packageManager.getPackageInfo(mContext.getPackageName(),0);
        } catch (NameNotFoundException e) {

        }
        return String.valueOf(packInfo.versionCode);
    }
    public static String getVersionName(Context cnt) {
        PackageManager packageManager = cnt.getPackageManager();
        PackageInfo packInfo = new PackageInfo();
        try {
            packInfo = packageManager.getPackageInfo(cnt.getPackageName(),0);
        } catch (NameNotFoundException e) {

        }
        return String.valueOf(packInfo.versionName);
    }

    protected void showUpdataDialog() {
        Builder builer = new Builder(mContext);
        builer.setTitle("版本升级");
        builer.setMessage("发现最新版,建议立即更新使用");
        builer.setPositiveButton("确定", new OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                DownloadManagerTool();
                if(listenr!= null){
                    listenr.onOk();
                }
            }

        });
        builer.setNegativeButton("取消", new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(listenr!= null){
                    listenr.onCancel();
                }
            }
        });
        AlertDialog dialog = builer.create();
        dialog.show();
    }

    public void start(){
        String versionCodeNow = getVersionCode();
        if (versionCodeNow.equals(serverVersion)) {
                    listenr.onCancel();
                } else {
                   try {
                       int cur  = Integer.parseInt(versionCodeNow);
                       int tar = Integer.parseInt(serverVersion);
                       if(cur<tar){
                           showUpdataDialog();
                       }else{
                           listenr.onCancel();
                       }
                   }catch (Exception e){
                       listenr.onCancel();
                   }

                }
    }



    protected void installApk(File file) {
        if (file.toString().endsWith(".apk")) {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file),
                    "application/vnd.android.package-archive");
            this.mContext.startActivity(intent);
        } else {


        }

    }


    class DownloadCompleteReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                installApk(file);

            }

        }

    }

}
