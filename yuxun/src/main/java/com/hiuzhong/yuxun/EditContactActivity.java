package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.baselib.listener.ImgUploadedListener;
import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.baselib.view.UploadImgView;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.vo.Contact;



public class EditContactActivity extends Activity implements ImgUploadedListener {
    private UploadImgView faceView;
    private Contact contact;
//    private Bitmap contactFaceBitmap;
    private TextView nickName;
    private TextView account;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_contact);
        faceView = (UploadImgView) findViewById(R.id.faceImg);
        nickName= (TextView) findViewById(R.id.userNickName);
        account = (TextView) findViewById(R.id.userAccount);
        initData();
    }

    private void initData(){
        ContactsDbManager dao = new ContactsDbManager(this);
        contact =dao.queryById(getIntent().getStringExtra("contactId"));
        faceView.setImageBitmap( ImageLoaderUtil.loadFromFile(this, contact.faceImgPath));
        nickName.setText(contact.nickName);
        account.setText(contact.account);
        faceView.faceImgFileName = contact.faceImgPath;
        faceView.parentActivity = this;
        faceView.uploadListener=this;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_edit_contact, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void SendMsg(View v){
        Intent intent = new Intent(this,ChatActivity.class);
        intent.putExtra("contactId", String.valueOf(contact.id));
        startActivity(intent);
        finish();
    }

    @Override
    public void onLoadFinish(Bitmap imgPath) {
        ImageLoaderUtil.saveBitmapToFile(this, contact.faceImgPath, imgPath);
        faceView.setTag("ok");
    }

    public void saveContactInfo(View view) {
        Contact c = new Contact();
        CharSequence nickName =this.nickName.getText();
        CharSequence account = this.account.getText();
        if (nickName == null) {
            tip("昵称不能为空");
            return;
        }
        if (account == null) {
            tip("账号不能为空");
            return;
        }
        ContactsDbManager manager = new ContactsDbManager(this);
        c.nickName = nickName.toString();
        c.account = account.toString();
        manager.updateAccount(c);
        manager.updateFaceImg(c);
        manager.updateNickName(c);
        tip("保存联系人成功");
        finish();
    }


    private void tip(String tip) {
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    public void delContact(View v){
        ContactsDbManager manager = new ContactsDbManager(this);
        manager.deleteContact(contact.account);
        manager.closeDB();
        Toast.makeText(this,"删除成功",Toast.LENGTH_SHORT).show();
        finish();
    }

    public void back(View v){
        finish();
    }

}
