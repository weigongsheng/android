package com.hiuzhong.yuxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiuzhong.yuxun.helper.ContactDBHelper;
import com.hiuzhong.yuxun.vo.Contact;
import com.hiuzhong.yuxun.vo.message.EntityContract;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class MsgCountDbManager {

    private static MsgCountDbManager instance;

    public static MsgCountDbManager getInstance(Context cnt){
        if(instance == null){
            instance = new MsgCountDbManager(cnt.getApplicationContext());
        }
        return instance;
    }


    public static String[] projection = {
            EntityContract.MSGCount._ID,
            EntityContract.MSGCount.COLUMN_NAME_ACCOUNT,
            EntityContract.MSGCount.COLUMN_NAME_COUNT
     };


    private ContactDBHelper helper;
    private SQLiteDatabase db;
    private MsgCountDbManager(Context context) {
        helper = new ContactDBHelper(context);
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
    private void closeDB() {
        db.close();
    }
    public static void close() {
       if(instance != null){
           instance.closeDB();
       }
    }

    public void clean(){
        db.execSQL("delete from " + EntityContract.MSGCount.TABLE_NAME);
    }
    public void clean(String account){
       db.delete(EntityContract.MSGCount.TABLE_NAME
                ,EntityContract.MSGCount.COLUMN_NAME_ACCOUNT+" = ?"
                ,new String[]{account});
    }

}
