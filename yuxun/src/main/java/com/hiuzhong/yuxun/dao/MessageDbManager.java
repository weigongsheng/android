package com.hiuzhong.yuxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiuzhong.yuxun.helper.ContactDBHelper;
import com.hiuzhong.yuxun.vo.Contact;
import com.hiuzhong.yuxun.vo.message.EntityContract;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class MessageDbManager {

    String[] projection = {
            EntityContract.MessageEntity._ID,
            EntityContract.MessageEntity.COLUMN_NAME_CONTENT,
            EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ID,
            EntityContract.MessageEntity.COLUMN_NAME_TYPE,
            EntityContract.MessageEntity.COLUMN_NAME_TIME,
     };


    private ContactDBHelper helper;
    private SQLiteDatabase db;
    public static  final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public MessageDbManager(Context context) {
        helper = new ContactDBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add message
     */
    public void add(int contactId,int msgType,String msg) {

        ContentValues values = new ContentValues();
        values.put(EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ID, contactId);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_TYPE, msgType);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_CONTENT, msg);
        values.put(EntityContract.MessageEntity.COLUMN_NAME_TIME, df.format(new Date()));
        db.insert(EntityContract.MessageEntity.TABLE_NAME, EntityContract.MessageEntity.COLUMN_NAME_NULLABLE, values);
    }




    public List<String[]> query(int pageSize,int startPage) {
        ArrayList<String[]> result = new ArrayList<String[]>();
        Cursor c = queryTheCursor(pageSize,startPage);
        String[] data =null;
        while (c.moveToNext()) {
            data= new String[projection.length];
            for (int i = 0; i <projection.length ; i++) {
                data[i]=c.getString(c.getColumnIndex(projection[i]));
            }

        }
        c.close();
        return result;
    }

    public Cursor queryTheCursor(int pageSize, int startPage) {
       return db.query(EntityContract.MessageEntity.TABLE_NAME, projection, " Limit " + pageSize + " Offset " + (startPage * pageSize), null, null, null, " order by " + EntityContract.MessageEntity._ID + " asc");
       // return  db.rawQuery("SELECT * FROM "+ EntityContract.MessageEntity.TABLE_NAME+" Limit "+pageSize+" Offset "+(startPage*pageSize), null);

    }


    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }

}
