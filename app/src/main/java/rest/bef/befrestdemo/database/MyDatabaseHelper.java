package rest.bef.befrestdemo.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import rest.bef.befrestdemo.SignupHelper;

/**
 * Created by hojjatimani on 12/22/2015 AD.
 */
public class MyDatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "MyDatabaseHelper";
    private static final String DB_NAME = "befrest.db";
    private static final int DB_VERSION = 1;


    public static final String TYPE_TEXT = " text";
    public static final String TYPE_INTEGER = " integer";
    public static final String TYPE_REAL = " real";
    public static final String COMMA_SEP = ", ";

    private static final String NUMBER_OF_TOPIC_MSGS = "SELECT COUNT(*) FROM "
            + ChatTable.TABLE_MESSAGES + " WHERE " + ChatTable.COLUMN_TOPIC + " IS NOT NULL";


    public MyDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ChatTable.onCreate(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        ChatTable.onUpgrade(db, oldVersion, newVersion);
    }

    public static  int getNumberOfAllTopicMessages(Context c) {
        SQLiteDatabase db = new MyDatabaseHelper(c).getReadableDatabase();
        Cursor cursor = db.query(ChatTable.TABLE_MESSAGES, null, ChatTable.COLUMN_TOPIC + " IS NOT NULL ", null, null, null, null);
        return cursor.getCount();
    }

    public static int getNumberOfChatsWithUser(Context c,String userId){
        SQLiteDatabase db = new MyDatabaseHelper(c).getReadableDatabase();
        String selection = null;
        if (SignupHelper.getUserId(c).equals(userId)) {
            selection = ChatTable.COLUMN_TOPIC + " IS NULL AND " + ChatTable.COLUMN_FROM + " = '" + userId + "' AND " + ChatTable.COLUMN_TO + " = '" + userId + "' ";
        } else {
            selection = ChatTable.COLUMN_TOPIC + " IS NULL AND (" + ChatTable.COLUMN_FROM + " = '" + userId + "' OR " + ChatTable.COLUMN_TO + " = '" + userId + "' )";
        }
        Cursor cursor = db.query(ChatTable.TABLE_MESSAGES, null, selection, null, null, null, null);
        return cursor.getCount();
    }
}