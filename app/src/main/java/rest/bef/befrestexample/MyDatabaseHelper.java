package rest.bef.befrestexample;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hojjatimani on 12/22/2015 AD.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyDatabaseHelper";

    public static final String DB_NAME = "befrest.db";
    public static final int DB_VERSION = 1;

    private static final String TYPE_TEXT = " msg";
    public static final String TYPE_INTEGER = " integer";
    public static final String TYPE_REAL = " real";

    private static final String COMMA_SEP = ", ";


    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FROM = "form";
    public static final String COLUMN_MSG = "msg";
    public static final String COLUMN_TIME_SENT = "time_sent";
    public static final String COLUMN_TIME_RECEIVED = "time_received";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_FROM, COLUMN_MSG, COLUMN_TIME_SENT, COLUMN_TIME_RECEIVED};

    private static final String CREATE_MESSAGES_TABLE =
            "create table " + TABLE_MESSAGES + "("
                    + COLUMN_ID + TYPE_INTEGER + " primary key autoincrement" + COMMA_SEP
                    + COLUMN_FROM + TYPE_TEXT + COMMA_SEP
                    + COLUMN_MSG + TYPE_TEXT + " not null" + COMMA_SEP
                    + COLUMN_TIME_SENT + TYPE_INTEGER + COMMA_SEP
                    + COLUMN_TIME_RECEIVED + TYPE_TEXT + " );";

    public MyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_MESSAGES_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(db);
    }

    public void insertMessage(rest.bef.befrestexample.Message msg) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FROM, msg.from);
        values.put(COLUMN_MSG, msg.msg);
        values.put(COLUMN_TIME_SENT, msg.timeSent);
        values.put(COLUMN_TIME_RECEIVED, msg.timeReceived);
        db.insert(TABLE_MESSAGES, null, values);
        db.close();
    }

    public List getAllMessages() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, ALL_COLUMNS, null, null, null, null, null);
        List<Message> messages = new ArrayList<>(cursor.getCount() + 10);
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            messages.add(cursorToMessage(cursor));
            cursor.moveToNext();
        }
        db.close();
        return messages;
    }

//    public Cursor getMessagesCursor() {
//        SQLiteDatabase db = getReadableDatabase();
//        return db.query(TABLE_MESSAGES, ALL_COLUMNS, null, null, null, null, null);
//    }

    private Message cursorToMessage(Cursor cursor) {
        Message res = new Message();
        cursor.getInt(0);
        res.from = cursor.getString(1);
        res.msg = cursor.getString(2);
        res.timeSent = cursor.getString(3);
        res.timeReceived = cursor.getLong(4);
        return res;
    }

    //
    public ArrayList<Message> getMessages(long from, long to) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, ALL_COLUMNS, COLUMN_TIME_RECEIVED + " > " + from + " AND " + COLUMN_TIME_RECEIVED + " < " + to, null, null, null, COLUMN_TIME_SENT);
        ArrayList<Message> messages = new ArrayList<>(cursor.getCount());
        cursor.moveToFirst();
        for (; !cursor.isAfterLast(); ) {
            messages.add(cursorToMessage(cursor));
            cursor.moveToNext();
        }
        db.close();
        return messages;
    }

    public ArrayList<Message> getMessagesReverse(long from, long to) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, ALL_COLUMNS, COLUMN_TIME_RECEIVED + " > " + from + " AND " + COLUMN_TIME_RECEIVED + " < " + to, null, null, null, COLUMN_TIME_SENT);
        ArrayList<Message> messages = new ArrayList<>(cursor.getCount());
        cursor.moveToLast();
        for (; !cursor.isBeforeFirst(); ) {
            messages.add(cursorToMessage(cursor));
            cursor.moveToPrevious();
        }
        db.close();
        return messages;
    }
//
    public int getNumberOfAllMessages() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_MESSAGES, null, null, null, null, null, null);
        int count = cursor.getCount();
        db.close();
        return count;
    }
}