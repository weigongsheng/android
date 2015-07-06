package com.hiuzhong.yuxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiuzhong.yuxun.helper.ContactDBHelper;
import com.hiuzhong.yuxun.vo.Contact;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class ContanctsDbManager {
    private ContactDBHelper helper;
    private SQLiteDatabase db;

    public ContanctsDbManager(Context context) {
        helper = new ContactDBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add persons
     * @param contacts
     */
    public void add(Contact... contacts) {
        db.beginTransaction();  //开始事务
        try {
            for (Contact person : contacts) {
                db.execSQL("INSERT INTO person VALUES(null, ?, ?, ?)", new Object[]{person.faceImgPath, person.nickName, person.account});
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    /**
     * update person's age
     * @param person
     */
    public void updateFaceImg(Contact person) {
        ContentValues cv = new ContentValues();
        cv.put(ContactDBHelper.COLUMN_FACE_IMG, person.faceImgPath);
        db.update("person", cv, ContactDBHelper.COLUMN_FACE_IMG + " = ?", new String[]{person.faceImgPath});
    }

    public boolean existAccount(String account){
        Cursor re = db.rawQuery("select " + ContactDBHelper.COLUMN_ID + " form " + ContactDBHelper.TABLE_NAME + " where "
                + ContactDBHelper.COLUMN_ACCOUNT + " =  '" + account + "'", null);
        return re.moveToNext();
    }
    /**
     * update person's age
     * @param person
     */
    public void updateNickName(Contact person) {
        ContentValues cv = new ContentValues();
        cv.put(ContactDBHelper.COLUMN_NICK_NAME, person.nickName);
        db.update(ContactDBHelper.TABLE_NAME, cv, ContactDBHelper.COLUMN_NICK_NAME + " = ?", new String[]{person.nickName});
    }
    /**
     * update person's age
     * @param person
     */
    public void updateAccount(Contact person) {
        ContentValues cv = new ContentValues();
        cv.put(ContactDBHelper.COLUMN_ACCOUNT, person.account);
        db.update(ContactDBHelper.TABLE_NAME, cv, ContactDBHelper.COLUMN_ACCOUNT+" = ?", new String[]{person.account});
    }

    public void deleteContact(String account){
        db.delete(ContactDBHelper.TABLE_NAME, ContactDBHelper.COLUMN_ACCOUNT + " = ?", new String[]{String.valueOf(account)});

    }

    public List<Contact> query() {
        ArrayList<Contact> contacts = new ArrayList<Contact>();
        Cursor c = queryTheCursor();
        while (c.moveToNext()) {
            Contact contact = new Contact();
            contact.id = c.getInt(c.getColumnIndex("_id"));
            contact.account = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_ACCOUNT));
            contact.nickName = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_NICK_NAME));
            contact.faceImgPath = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_FACE_IMG));
            contacts.add(contact);
        }
        c.close();
        return contacts;
    }

    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM "+ ContactDBHelper.TABLE_NAME, null);
        return c;
    }

    /**
     * close database
     */
    public void closeDB() {
        db.close();
    }

}
