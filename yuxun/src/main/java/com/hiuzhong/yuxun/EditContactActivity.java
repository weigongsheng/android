package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.baselib.listener.ImgUploadedListener;
import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.baselib.view.UploadImgView;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.vo.Contact;


public class EditContactActivity extends Activity implements ImgUploadedListener {

    private Bitmap changedHead;
    protected UploadImgView faceView;
    private Contact contact;
    protected EditText account;
    protected EditText nickName;
    protected Button submitBtn;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        faceView = (UploadImgView) findViewById(R.id.faceImg);
        faceView.parentActivity = this;
        faceView.uploadListener = this;
        ActivityHelper.initHeadInf(this);
        account = (EditText) findViewById(R.id.userAccount);
        nickName = (EditText) findViewById(R.id.userNickName);
        submitBtn = (Button) findViewById(R.id.button);
        submitBtn.setText("保存");
        loadContact();
        faceView.faceImgFileName = contact.faceImgPath== null? "" + System.currentTimeMillis() + ".jpg":contact.faceImgPath;
    }

    private void loadContact() {
        ContactsDbManager manager = ContactsDbManager.getInstance(this);
        contact = manager.queryById(getIntent().getStringExtra("contactId"));
        if (contact.faceImgPath != null) {
            Bitmap img = ImageLoaderUtil.loadFromFile(this, contact.faceImgPath);
            if (img != null) {
                faceView.setImageBitmap(img);
            }
        }
        account.setText(contact.account);
        nickName.setText(contact.nickName);
    }

    @Override
    public void onLoadFinish(Bitmap imgPath) {
        changedHead = imgPath;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        faceView.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }


    public void saveContactInfo(View view) {
        CharSequence nickName = ((TextView) findViewById(R.id.userNickName)).getText();
        CharSequence account = ((TextView) findViewById(R.id.userAccount)).getText();

        if (nickName == null) {
            tip("昵称不能为空");
            return;
        }
        if (account == null) {
            tip("账号不能为空");
            return;
        }

        ContactsDbManager manager = ContactsDbManager.getInstance(this);
        contact.nickName = nickName.toString();
        contact.account = account.toString();
        ActivityHelper.contactChanged();
        if (changedHead != null) {
            ImageLoaderUtil.saveBitmapToFile(this, faceView.faceImgFileName, changedHead);
            changedHead.recycle();
        }
        manager.update(contact);
        finish();
    }

    private void tip(String tip) {
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }
}
