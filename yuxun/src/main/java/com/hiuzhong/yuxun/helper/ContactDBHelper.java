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
        //CursorFactory����Ϊnull,ʹ��Ĭ��ֵ  
        super(context, DATABASE_NAME, null, DATABASE_VERSION);  
    }  
  
    //���ݿ��һ�α�����ʱonCreate�ᱻ����  
    @Override  
    public void onCreate(SQLiteDatabase db) {  
        db.execSQL("CREATE TABLE IF NOT EXISTS " +TABLE_NAME+
                "("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+COLUMN_FACE_IMG+" varchar," +
                COLUMN_NICK_NAME+" varchar, "+COLUMN_ACCOUNT+" VARCHAR)");
    }  
  
    //���DATABASE_VERSIONֵ����Ϊ2,ϵͳ�����������ݿ�汾��ͬ,�������onUpgrade  
    @Override  
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {  
        db.execSQL("ALTER TABLE contacts ADD COLUMN other STRING");
    }  
}  