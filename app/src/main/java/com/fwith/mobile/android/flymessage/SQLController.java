package com.fwith.mobile.android.flymessage;

/**
 * Created by fwith on 15. 4. 2..
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SQLController {

    private DBhelper dbhelper;
    private Context ourcontext;
    private SQLiteDatabase database;

    public SQLController(Context c) {
        ourcontext = c;
    }

    public SQLController open() throws SQLException {
        dbhelper = new DBhelper(ourcontext);
        database = dbhelper.getWritableDatabase();
        return this;
    }

    public void close() {
        dbhelper.close();
    }

    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }
    //Inserting Data into table
    public void insertData(String phoneNumber, String contents) {
        ContentValues cv = new ContentValues();
        cv.put(DBhelper.SMS_PHONENUMBER, phoneNumber);
        cv.put(DBhelper.SMS_CONTENTS, contents);
        cv.put(DBhelper.SMS_DATE, getDateTime());
        database.insert(DBhelper.TABLE_SMS, null, cv);
    }

    //Getting Cursor to read data from table
    public Cursor readData() {
        String orderBy =  DBhelper.SMS_DATE + " DESC";
        String[] allColumns = new String[] { DBhelper.SMS_ID, DBhelper.SMS_DATE, DBhelper.SMS_PHONENUMBER, DBhelper.SMS_CONTENTS };
        Cursor c = database.query(DBhelper.TABLE_SMS, allColumns, null, null, null, null, orderBy);
        if (c != null) {
            c.moveToFirst();
        }
        return c;
    }

    //Updating record data into table by id
//    public int updateData(long memberID, String memberName) {
//        ContentValues cvUpdate = new ContentValues();
//        cvUpdate.put(DBhelper.SMS_PHONENUMBER, memberName);
//        int i = database.update(DBhelper.TABLE_MEMBER, cvUpdate,
//                DBhelper.MEMBER_ID + " = " + memberID, null);
//        return i;
//    }

    // Deleting record data from table by id
//    public void deleteData(long memberID) {
//        database.delete(DBhelper.TABLE_MEMBER, DBhelper.MEMBER_ID + "="
//                + memberID, null);
//    }

}
