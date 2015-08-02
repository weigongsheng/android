package com.hiuzhong.yuxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiuzhong.yuxun.helper.ContactDBHelper;
import com.hiuzhong.yuxun.vo.message.EntityContract;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class MsgCountDbManager {

    public static String[] projection = {
            EntityContract.MSGCount._ID,
            EntityContract.MSGCount.COLUMN_NAME_ACCOUNT,
            EntityContract.MSGCount.COLUMN_NAME_COUNT
     };


    private ContactDBHelper helper;
    private SQLiteDatabase db;
    public MsgCountDbManager(Context context) {
        helper = new ContactDBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add message
     */
    public void add(String contractAccount ,int count ) {
        Integer oc = queryCount(contractAccount);
        if(oc == null){
            insert(contractAccount, 1);
        }else{
         update(contractAccount, oc+count);
        }
    }

    private int update(String contractAccount, int i) {
        ContentValues cv = new ContentValues();
        cv.put(EntityContract.MSGCount.COLUMN_NAME_COUNT, i);
        return  db.update(EntityContract.MSGCount.TABLE_NAME, cv, EntityContract.MSGCount.COLUMN_NAME_ACCOUNT + " = ?", new String[]{contractAccount});
    }

    private void insert(String contractAccount, int i) {
        ContentValues cv = new ContentValues();
        cv.put(EntityContract.MSGCount.COLUMN_NAME_COUNT, i);
        cv.put(EntityContract.MSGCount.COLUMN_NAME_ACCOUNT, contractAccount);
        db.insert(EntityContract.MSGCount.TABLE_NAME, null, cv);
    }


    public Integer queryCount(String account){
        Cursor query = db.query(EntityContract.MSGCount.TABLE_NAME, projection, EntityContract.MSGCount.COLUMN_NAME_ACCOUNT + " =?"
                , new String[]{account}, null, null, null);
        if (query.moveToNext()){
            return query.getInt(query.getColumnIndex((EntityContract.MSGCount.COLUMN_NAME_COUNT)));
        }
        return null;
    }

    public void cleanCount(String account) {
        update(account,0);
    }
    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }

}
