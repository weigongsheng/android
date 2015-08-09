package com.hiuzhong.yuxun.dao;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hiuzhong.yuxun.helper.ContactDBHelper;
import com.hiuzhong.yuxun.vo.Contact;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by gongsheng on 2015/7/5.
 */
public class ContactsDbManager {
    private static ContactsDbManager instance;
    private ContactDBHelper helper;
    private SQLiteDatabase db;
    public String[] projection = new String[]{
            ContactDBHelper.COLUMN_ID,
            ContactDBHelper.COLUMN_ACCOUNT,
            ContactDBHelper.COLUMN_NICK_NAME,
            ContactDBHelper.COLUMN_FACE_IMG,
            ContactDBHelper.COLUMN_FIRST_CHAR,
    };

    public static ContactsDbManager getInstance(Context cnt) {
        if (instance == null) {
            instance = new ContactsDbManager(cnt.getApplicationContext());
        }
        return instance;
    }

    public static void close() {
        if (instance != null) {
            instance.closeDB();
            instance = null;
        }
    }

    private ContactsDbManager(Context context) {
        helper = new ContactDBHelper(context);
        //因为getWritableDatabase内部调用了mContext.openOrCreateDatabase(mName, 0, mFactory);
        //所以要确保context已初始化,我们可以把实例化DBManager的步骤放在Activity的onCreate里
        db = helper.getWritableDatabase();
    }

    /**
     * add persons
     *
     * @param contacts
     */
    public void add(Contact... contacts) {
        db.beginTransaction();  //开始事务
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        try {
            for (Contact person : contacts) {
                person.firstChar = String.valueOf(getFirstChar(person.nickName.charAt(0)));
                db.execSQL("INSERT INTO " + ContactDBHelper.TABLE_CONTACTS_NAME + " VALUES(null, ?, ?, ?,?)", new Object[]{person.faceImgPath, person.nickName, person.account, person.firstChar});
            }
            db.setTransactionSuccessful();  //设置事务成功完成
        } finally {
            db.endTransaction();    //结束事务
        }
    }

    public static char getFirstChar(char c) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        try {
            String[] fc = PinyinHelper.toHanyuPinyinStringArray(c, format);
            if (fc != null && fc[0] != null) {
                return fc[0].charAt(0);
            }
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }
        return c;
    }

    /**
     * update person's age
     *
     * @param person
     */
    public void updateFaceImg(Contact person) {
        ContentValues cv = new ContentValues();
        cv.put(ContactDBHelper.COLUMN_FACE_IMG, person.faceImgPath);
        db.update(ContactDBHelper.TABLE_CONTACTS_NAME, cv, ContactDBHelper.COLUMN_ACCOUNT + " = ?", new String[]{person.account});
    }

    public boolean existAccount(String account) {
        Cursor re = db.rawQuery("select " + ContactDBHelper.COLUMN_ID + " from " + ContactDBHelper.TABLE_CONTACTS_NAME + " where "
                + ContactDBHelper.COLUMN_ACCOUNT + " =  '" + account + "'", null);
        return re.moveToNext();
    }

    /**
     * @param person
     */
    public void updateNickName(Contact person) {
        ContentValues cv = new ContentValues();
        person.firstChar = String.valueOf(getFirstChar(person.nickName.charAt(0)));
        cv.put(ContactDBHelper.COLUMN_NICK_NAME, person.nickName);
        cv.put(ContactDBHelper.COLUMN_FIRST_CHAR, "" + person.firstChar);
        db.update(ContactDBHelper.TABLE_CONTACTS_NAME, cv, ContactDBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(person.id)});
    }

    /**
     * update person's age
     *
     * @param person
     */
    public void updateAccount(Contact person) {
        ContentValues cv = new ContentValues();
        cv.put(ContactDBHelper.COLUMN_ACCOUNT, person.account);
        db.update(ContactDBHelper.TABLE_CONTACTS_NAME, cv, ContactDBHelper.COLUMN_ID + " = ?",
                new String[]{String.valueOf(person.id
        )});
    }

    public void deleteContact(String account) {
        db.delete(ContactDBHelper.TABLE_CONTACTS_NAME, ContactDBHelper.COLUMN_ACCOUNT + " = ?",
                new String[]{String.valueOf(account)});

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
            contact.firstChar = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_FIRST_CHAR));
            contacts.add(contact);
        }
        c.close();
        return contacts;
    }

    public Contact queryById(String id) {
        Cursor c = db.query(
                ContactDBHelper.TABLE_CONTACTS_NAME,  // The table to query
                projection,                               // The columns to return
                ContactDBHelper.COLUMN_ID + " =? ",                                // The columns for the WHERE clause
                new String[]{id},                            // The values for the WHERE clause
                null,                                     //  group the rows
                null,                                     // don't filter by row groups
                null
        );
        if (c.moveToNext()) {
            Contact contact = new Contact();
            contact.id = c.getInt(c.getColumnIndex(ContactDBHelper.COLUMN_ID));
            contact.account = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_ACCOUNT));
            contact.nickName = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_NICK_NAME));
            contact.faceImgPath = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_FACE_IMG));
            contact.firstChar = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_FIRST_CHAR));
            return contact;
        }
        return null;
    }

    public Contact queryByAccount(String account) {
        Cursor c = db.query(
                ContactDBHelper.TABLE_CONTACTS_NAME,  // The table to query
                projection,                               // The columns to return
                ContactDBHelper.COLUMN_ACCOUNT + " =? ",                                // The columns for the WHERE clause
                new String[]{account},                             // The values for the WHERE clause
                null,                                     //  group the rows
                null,                                     // don't filter by row groups
                null
        );
        if (c.moveToNext()) {
            Contact contact = new Contact();
            contact.id = c.getInt(c.getColumnIndex(ContactDBHelper.COLUMN_ID));
            contact.account = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_ACCOUNT));
            contact.nickName = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_NICK_NAME));
            contact.faceImgPath = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_FACE_IMG));
            contact.firstChar = c.getString(c.getColumnIndex(ContactDBHelper.COLUMN_FIRST_CHAR));
            return contact;
        }
        return null;
    }

    public Cursor queryTheCursor() {
        Cursor c = db.rawQuery("SELECT * FROM " + ContactDBHelper.TABLE_CONTACTS_NAME, null);

        return c;
    }


    /**
     * close database
     */
    private void closeDB() {
        db.close();
    }

    public void update(Contact person) {
//        updateNickName(contact);
//        updateAccount(contact);
        ContentValues cv = new ContentValues();
        person.firstChar = String.valueOf(getFirstChar(person.nickName.charAt(0)));
        cv.put(ContactDBHelper.COLUMN_ACCOUNT, person.account);
        cv.put(ContactDBHelper.COLUMN_NICK_NAME, person.nickName);
        cv.put(ContactDBHelper.COLUMN_FIRST_CHAR, "" + person.firstChar);
        cv.put(ContactDBHelper.COLUMN_FACE_IMG, "" + person.faceImgPath);
        db.update(ContactDBHelper.TABLE_CONTACTS_NAME, cv, ContactDBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(person.id)});
    }
}
