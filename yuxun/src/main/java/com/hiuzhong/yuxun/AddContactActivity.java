package com.hiuzhong.yuxun;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hiuzhong.baselib.listener.ImgUploadedListener;
import com.hiuzhong.baselib.util.ImageLoaderUtil;
import com.hiuzhong.baselib.view.UploadImgView;
import com.hiuzhong.yuxun.dao.ContactsDbManager;
import com.hiuzhong.yuxun.helper.ActivityHelper;
import com.hiuzhong.yuxun.vo.Contact;


public class AddContactActivity extends Activity implements ImgUploadedListener {
    protected UploadImgView faceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_contact);
        faceView = (UploadImgView) findViewById(R.id.faceImg);
        faceView.parentActivity = this;
        faceView.faceImgFileName = "" + System.currentTimeMillis() + ".jpg";
        faceView.uploadListener=this;
        ActivityHelper.initHeadInf(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_add_contact, menu);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        faceView.onActivityResult(requestCode, resultCode, data);
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onLoadFinish(Bitmap img) {
        ImageLoaderUtil.saveBitmapToFile(this, faceView.faceImgFileName, img);
        faceView.setTag("ok");
    }

    public void saveContactInfo(View view) {
        Contact c = new Contact();
        c.faceImgPath = faceView.faceImgFileName;
        CharSequence nickName = ((TextView) findViewById(R.id.userNickName)).getText();
        CharSequence account = ((TextView) findViewById(R.id.userAccount)).getText();
        if (faceView.getTag() == null || !faceView.getTag().equals("ok")) {
//            tip(".请上传头像");
            ImageLoaderUtil.saveBitmapToFile(this, faceView.faceImgFileName, BitmapFactory.decodeResource(getResources(),R.drawable.default_head));
            faceView.setTag("ok");
        }
        if (nickName == null || nickName.toString().trim().length()<1) {
            tip("昵称不能为空");
            return;
        }
        if (account == null || account.toString().trim().length()<1) {
            tip("账号不能为空");
            return;
        }
        ContactsDbManager manager =   ContactsDbManager.getInstance(this);
        if (manager.existAccount(account.toString())) {
            tip("账号已存在");
            return;
        }
        c.nickName = nickName.toString();
        c.account = account.toString();
        manager.add(c);
        ActivityHelper.contactChanged();
        tip("添加联系人成功");
        finish();
    }


    private void tip(String tip) {
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    public void back(View v){
        finish();
    }
}
