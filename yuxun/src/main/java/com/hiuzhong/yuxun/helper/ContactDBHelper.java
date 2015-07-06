package com.hiuzhong.yuxun.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;  
import android.database.sqlite.SQLiteOpenHelper;  
  
public class ContactDBHelper extends SQLiteOpenHelper {
  
    private static final String DATABASE_NAME = "yuxun.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_NAME="contacts";
    public static final String COLUMN_FACE_IMG="face_img";
    public static final String COLUMN_ID="_id";
    public static final String COLUMN_NICK_NAME="nick_name";
    public static final String COLUMN_ACCOUNT="account";


    public ContactDBHelper(Context context) {
        //CursorFactory设置为null,使用默认值  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }  
  
    //数据库第一次被创建时onCreate会被调用  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        db.execSQL("CREATE TABLE IF NOT EXISTS " +TABLE_NAME+
                "("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+COLUMN_FACE_IMG+" varchar," +
                COLUMN_NICK_NAME+" varchar, "+COLUMN_ACCOUNT+" VARCHAR)");
    }  
  
    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL("ALTER TABLE contacts ADD COLUMN other STRING");
    }  
}  