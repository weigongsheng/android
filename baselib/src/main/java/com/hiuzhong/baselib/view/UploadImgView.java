package com.hiuzhong.baselib.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.Toast;

import com.hiuzhong.baselib.listener.ImgUploadedListener;
import com.hiuzhong.baselib.util.ToolsUtil;

import java.io.File;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class UploadImgView extends ImageView {
    /* 请求码 */
    private static final int IMAGE_REQUEST_CODE = 0x1;
    private static final int CAMERA_REQUEST_CODE = 0x2;
    private static final int RESULT_REQUEST_CODE = 0x3;

    public Activity parentActivity;
    public String faceImgFileName;
    public ImgUploadedListener uploadListener;
    private String[] items = new String[] { "选择本地图片", "拍照" };
    //    private static final String IMAGE_FILE_NAME = "faceImage.jpg";
    public UploadImgView(Context context) {
        super(context);
        init();
    }

    public UploadImgView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public UploadImgView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected  void init(){
        setFocusable(true);
        setClickable(true);
    }

    private void showDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("设置头像")
                .setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                Intent intentFromGallery = new Intent();
                                intentFromGallery.setType("image/*"); // 设置文件类型
                                intentFromGallery
                                        .setAction(Intent.ACTION_GET_CONTENT);
                                intentFromGallery.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                parentActivity.startActivityForResult(intentFromGallery,IMAGE_REQUEST_CODE);
                                break;
                            case 1:
                                Intent intentFromCapture = new Intent(
                                        MediaStore.ACTION_IMAGE_CAPTURE);
                                // 判断存储卡是否可以用，可用进行存储
                                if (ToolsUtil.hasSdcard()) {
                                    File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                                    File file = new File(path,faceImgFileName);
                                    intentFromCapture.putExtra(
                                            MediaStore.EXTRA_OUTPUT,
                                            Uri.fromFile(file));
                                }

                                parentActivity.startActivityForResult(intentFromCapture, CAMERA_REQUEST_CODE);
                                break;
                        }
                    }
                })
                .setNegativeButton("取消", null).show();

    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        //结果码不等于取消时候
        if (resultCode != parentActivity.RESULT_CANCELED) {

            switch (requestCode) {
                case IMAGE_REQUEST_CODE:
                    startPhotoZoom(data.getData());
                    break;
                case CAMERA_REQUEST_CODE:
                    if (ToolsUtil.hasSdcard()) {
                        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                        File tempFile = new File(path,faceImgFileName);
                        startPhotoZoom(Uri.fromFile(tempFile));
                    } else {
                        Toast.makeText(getContext(), "未找到存储卡，无法存储照片！", Toast.LENGTH_LONG).show();
                    }
                    break;
                case RESULT_REQUEST_CODE: //图片缩放完成后
                    if (data != null) {
                        getImageToView(data);
                    }
                    break;
            }
        }
    }

    /**
     * 裁剪图片方法实现
     *
     * @param uri
     */
    public void startPhotoZoom(Uri uri) {

        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(uri, "image/*");
        // 设置裁剪
        intent.putExtra("crop", "true");
        // aspectX aspectY 是宽高的比例
        intent.putExtra("aspectX", 1);
        intent.putExtra("aspectY", 1);
        // outputX outputY 是裁剪图片宽高
        intent.putExtra("outputX", 240);
        intent.putExtra("outputY", 240);
        intent.putExtra("return-data", true);
        parentActivity.startActivityForResult(intent, RESULT_REQUEST_CODE);
    }


    /**
     * 保存裁剪之后的图片数据
     *
     * @param data
     */
    private void getImageToView(Intent data) {
        Bundle extras = data.getExtras();
        if (extras != null) {
            Bitmap photo = extras.getParcelable("data");
            Drawable drawable = new BitmapDrawable(this.getResources(),photo);
            setImageDrawable(drawable);
            if(uploadListener != null){
                uploadListener.onLoadFinish(photo);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(event.getAction() ==MotionEvent.ACTION_DOWN){
          showDialog();
         return true;
        }
        return false;
    }


}
