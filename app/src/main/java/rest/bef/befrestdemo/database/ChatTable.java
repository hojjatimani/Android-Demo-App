package rest.bef.befrestdemo.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static rest.bef.befrestdemo.database.MyDatabaseHelper.*;

/**
 * Created by hojjatimani on 1/24/2016 AD.
 */
public class ChatTable {

    public static final String TABLE_MESSAGES = "messages";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FROM = "fromId";
    public static final String COLUMN_TO = "toId";
    public static final String COLUMN_TOPIC = "topic";
    public static final String COLUMN_MSG = "msg";
    public static final String COLUMN_TIME = "time_sent";
    public static final String COLUMN_SENT = "status";
    public static final String COLUMN_IS_INCOMING = "isIncoming";

    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_FROM, COLUMN_TO, COLUMN_TOPIC, COLUMN_MSG,
            COLUMN_TIME, COLUMN_SENT, COLUMN_IS_INCOMING};

    private static final String CREATE_MESSAGES_TABLE =
            "create table " + TABLE_MESSAGES + "("
                    + COLUMN_ID + TYPE_INTEGER + " primary key autoincrement" + COMMA_SEP
                    + COLUMN_FROM + TYPE_TEXT + COMMA_SEP
                    + COLUMN_TO + TYPE_TEXT + COMMA_SEP
                    + COLUMN_TOPIC + TYPE_TEXT + COMMA_SEP
                    + COLUMN_MSG + TYPE_TEXT + " not null" + COMMA_SEP
                    + COLUMN_TIME + TYPE_INTEGER + COMMA_SEP
                    + COLUMN_SENT + TYPE_INTEGER + COMMA_SEP
                    + COLUMN_IS_INCOMING + TYPE_INTEGER + " );";


    public static void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_MESSAGES_TABLE);
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion,
                                 int newVersion) {
        Log.w(ChatTable.class.getName(), "Upgrading database from version "
                + oldVersion + " to " + newVersion
                + ", which will destroy all old data");
        database.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGES);
        onCreate(database);
    }

}
