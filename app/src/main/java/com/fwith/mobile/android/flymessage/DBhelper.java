package com.fwith.mobile.android.flymessage;

/**
 * Created by fwith on 15. 4. 2..
 */
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBhelper extends SQLiteOpenHelper {

    // TABLE INFORMATTION
    public static final String TABLE_SMS = "sms";
    public static final String SMS_ID = "_id";
    public static final String SMS_DATE = "date";
    public static final String SMS_PHONENUMBER = "phonenum";
    public static final String SMS_CONTENTS = "contents";

    // DATABASE INFORMATION
    static final String DB_NAME = "SMS.DB";
    static final int DB_VERSION = 1;

    // TABLE CREATION STATEMENT
    private static final String CREATE_TABLE = "create table "
            + TABLE_SMS + "(" + SMS_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + SMS_DATE + " DEFAULT CURRENT_TIMESTAMP, "
            + SMS_PHONENUMBER + " TEXT NOT NULL, "
            + SMS_CONTENTS + " TEXT NOT NULL);";

    public DBhelper(Context context) {
        super(context, DB_NAME, null,DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SMS);
        onCreate(db);
    }
}