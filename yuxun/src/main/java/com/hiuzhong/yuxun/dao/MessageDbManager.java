package com.hiuzhong.yuxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiuzhong.yuxun.helper.ContactDBHelper;
import com.hiuzhong.yuxun.vo.message.EntityContract;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class MessageDbManager {
    public static final String MSG_TYPE_SEND="1";
    public static final String MSG_TYPE_RECEIVE="2";

    public static String[] projection = {
            EntityContract.MessageEntity._ID,
            EntityContract.MessageEntity.COLUMN_NAME_CONTENT,
            EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ACCOUNT,
            EntityContract.MessageEntity.COLUMN_NAME_TYPE,
            EntityContract.MessageEntity.COLUMN_NAME_TIME,
            EntityContract.MessageEntity.COLUMN_NAME_FLAG
     };


    private ContactDBHelper helper;
    private SQLiteDatabase db;
    public MessageDbManager(Context context) {
        helper = new ContactDBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add message
     */
    public void add(String contractAccount,String msgType,String msg) {

        ContentValues values = new ContentValues();
        values.put(EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ACCOUNT, contractAccount);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_TYPE, msgType);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_CONTENT, msg);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_FLAG, 1);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_TIME, ContactDBHelper.df.format(new Date()));
        db.insert(EntityContract.MessageEntity.TABLE_NAME, EntityContract.MessageEntity.COLUMN_NAME_NULLABLE, values);
    }
    /**
     * add message
     */
    public void addNewMsg(String contAccount,String msgType,String msg,String time) {
        ContentValues values = new ContentValues();
        values.put(EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ACCOUNT, contAccount);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_TYPE, msgType);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_CONTENT, msg);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_FLAG, 0);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_TIME, time);
        db.insert(EntityContract.MessageEntity.TABLE_NAME, EntityContract.MessageEntity.COLUMN_NAME_NULLABLE, values);
    }




    public List<String[]> query(int pageSize,int startPage,String contactAccount) {
        ArrayList<String[]> result = new ArrayList<String[]>();
        Cursor c = queryTheCursor(pageSize,startPage,contactAccount);
        String[] data =null;
        while (c.moveToNext()) {
            data= new String[projection.length];
            for (int i = 0; i <projection.length ; i++) {
                data[i]=c.getString(c.getColumnIndex(projection[i]));
            }
            result.add(data);
        }
        c.close();
        return result;
    }

    public Cursor queryTheCursor(int pageSize, int startPage,String contactAccount) {
        String whereCause = null;
        String[] paras= null;
        if (contactAccount != null){
            whereCause=EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ACCOUNT+" =? ";
            paras = new String[]{contactAccount};
        }//db.rawQuery("select * from message where _id =1 order by _id desc Limit 3 offset 0",null);
       return db.query(EntityContract.MessageEntity.TABLE_NAME, projection, whereCause
               , paras, null, null,  EntityContract.MessageEntity._ID + " desc Limit " + pageSize + " Offset " + (startPage * pageSize));
       // return  db.rawQuery("SELECT * FROM "+ EntityContract.MessageEntity.TABLE_NAME+" Limit "+pageSize+" Offset "+(startPage*pageSize), null);

    }


    /**
     * 获取联系人最后一条聊天记录,并按照id倒排序
     * @return
     */
    public List<String[]> queryLastMsg(){
        String maxId= "max("+ EntityContract.MessageEntity._ID+")";
        Cursor c = db.query(
                EntityContract.MessageEntity.TABLE_NAME,  // The table to query
                new String[]{maxId, EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ACCOUNT},// The columns to return
                null,                                // The columns for the WHERE clause
                null,                            // The values for the WHERE clause
                EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ACCOUNT,                                     //  group the rows
                null,                                     // don't filter by row groups
                maxId + " desc"                                 // The sort order
        );
        List<String[]> re = new ArrayList<>();
        while (c.moveToNext()){
            String[] inf = new String[2];
            inf[0]= c.getString(0);
            inf[1]= c.getString(1);
            re.add(inf);
        }
        return re;
    }

    public String[] queryMsgById(String id){
        Cursor c = db.query(
                EntityContract.MessageEntity.TABLE_NAME,  // The table to query
                projection,                               // The columns to return
                EntityContract.MessageEntity._ID+" =? ",                                // The columns for the WHERE clause
                new String[]{id},                            // The values for the WHERE clause
                null,                                     //  group the rows
                null,                                     // don't filter by row groups
                null
        );
        if(c.moveToNext()){
            String[] re = new String[projection.length];
            for (int i = 0; i <projection.length ; i++) {
                re[i]=c.getString(i);
            }
            return re;
        }
        return null;
    }

    public Map<String,Integer> queryUnreadCound(){


        return null;
    }


    public void cleanMsg(){
        db.execSQL("delete  from "+EntityContract.MessageEntity.TABLE_NAME);
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }

}
