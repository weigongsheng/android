package com.hiuzhong.yuxun.vo.message;

import android.provider.BaseColumns;

/**
 * Created by gongsheng on 2015/7/9.
 */
public final class EntityContract {
    public EntityContract() {}
    public static abstract class MessageEntity implements BaseColumns {
        public static final String TABLE_NAME = "message";
        public static final String COLUMN_NAME_CONTACT_ACCOUNT = "contactAccount";
        public static final String COLUMN_NAME_CONTENT = "content";
        public static final String COLUMN_NAME_TYPE = "type";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_FLAG ="flag";

        public static final String COLUMN_NAME_NULLABLE =null;
    }
}
