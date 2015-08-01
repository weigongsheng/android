package com.hiuzhong.yuxun.helper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hiuzhong.yuxun.vo.message.EntityContract;

import java.text.SimpleDateFormat;

public class ContactDBHelper extends SQLiteOpenHelper {
    public static  final SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private static final String DATABASE_NAME = "yuxun.db";
    private static final int DATABASE_VERSION = 7;


    private static final String TEXT_TYPE = " TEXT";
    private static final String INTEGER_TYPE = " INTEGER";
    private static final String COMMA_SEP = ",";
    public static final String TABLE_CONTACTS_NAME ="contacts";
    public static final String COLUMN_FACE_IMG="face_img";
    public static final String COLUMN_ID="_id";
    public static final String COLUMN_NICK_NAME="nick_name";
    public static final String COLUMN_ACCOUNT="account";
    public static final String COLUMN_FIRST_CHAR="first_char";
    public static final String SQL_DELETE_ENTRIES ="DROP TABLE IF EXISTS " + TABLE_CONTACTS_NAME;
    public static final String SQL_DELETE_MESSAGE ="DROP TABLE IF EXISTS " + EntityContract.MessageEntity.TABLE_NAME;

    public static final String CREATE_TABLE_MESSAGE= "CREATE TABLE IF NOT EXISTS " + EntityContract.MessageEntity.TABLE_NAME + " (" +
            EntityContract.MessageEntity._ID + " INTEGER PRIMARY KEY," +
            EntityContract.MessageEntity.COLUMN_NAME_CONTENT + TEXT_TYPE +" not null"+ COMMA_SEP +
            EntityContract.MessageEntity.COLUMN_NAME_CONTACT_ACCOUNT  + INTEGER_TYPE +" not null"+ COMMA_SEP +
            EntityContract.MessageEntity.COLUMN_NAME_TYPE + TEXT_TYPE+" not null" + COMMA_SEP +
            EntityContract.MessageEntity.COLUMN_NAME_TIME + TEXT_TYPE    + COMMA_SEP +
            EntityContract.MessageEntity.COLUMN_NAME_FLAG + TEXT_TYPE +
            " )";

    public ContactDBHelper(Context context) {
        //CursorFactory设置为null,使用默认值
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    //数据库第一次被创建时onCreate会被调用
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CONTACTS_NAME +
                "("+COLUMN_ID+" INTEGER PRIMARY KEY AUTOINCREMENT,"+COLUMN_FACE_IMG+" varchar," +
                COLUMN_NICK_NAME+" varchar, "+COLUMN_ACCOUNT+" VARCHAR, "+COLUMN_FIRST_CHAR+" VARCHAR ) ");
        db.execSQL(CREATE_TABLE_MESSAGE);
    }

    //如果DATABASE_VERSION值被改为2,系统发现现有数据库版本不同,即会调用onUpgrade
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_DELETE_MESSAGE);
        onCreate(db);

    }
}