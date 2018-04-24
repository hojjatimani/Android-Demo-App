package rest.bef.befrestdemo.database;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Created by hojjatimani on 4/1/2016 AD.
 */
public class ChatContentProvider extends ContentProvider {

    // database
    private MyDatabaseHelper database;

    // used for the UriMacher
    private static final int CHATS = 10;
    private static final int CHAT_ID = 20;

    private static final String AUTHORITY = "rest.bef.befrestdemo.chat.contentprovider";

    private static final String BASE_PATH = "chats";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
            + "/" + BASE_PATH);

    public static final String CONTENT_TYPE = ContentResolver.CURSOR_DIR_BASE_TYPE
            + "/chats";
    public static final String CONTENT_ITEM_TYPE = ContentResolver.CURSOR_ITEM_BASE_TYPE
            + "/chat";

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, CHATS);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", CHAT_ID);
    }

    public static Uri getContentUriWithId(int id) {
        return Uri.parse("content://" + AUTHORITY
                + "/" + BASE_PATH + "/" + id);
    }

    @Override
    public boolean onCreate() {
        database = new MyDatabaseHelper(getContext());
        return false;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        // Uisng SQLiteQueryBuilder instead of query() method
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();

        // check if the caller has requested a.js column which does not exists
        checkColumns(projection);

        // Set the table
        queryBuilder.setTables(ChatTable.TABLE_MESSAGES);

        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case CHATS:
                break;
            case CHAT_ID:
                // adding the ID to the original query
                queryBuilder.appendWhere(ChatTable.COLUMN_ID + "="
                        + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        SQLiteDatabase db = database.getWritableDatabase();
        Cursor cursor = queryBuilder.query(db, projection, selection,
                selectionArgs, null, null, sortOrder);

//        String query = "select * from (select * from " + ChatTable.TABLE_CHATS +" order by " +sortOrder+ ") order by time DESC;";
//        Cursor cursor = db.rawQuery(query, null);
//        "select * from (select * from tblmessage order by sortfield ASC limit 10) order by sortfield DESC"


        // make sure that potential listeners are getting notified
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        long id = 0;
        switch (uriType) {
            case CHATS:
                id = sqlDB.insert(ChatTable.TABLE_MESSAGES, null, values);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsDeleted = 0;
        switch (uriType) {
            case CHATS:
                rowsDeleted = sqlDB.delete(ChatTable.TABLE_MESSAGES, selection,
                        selectionArgs);
                break;
            case CHAT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(ChatTable.TABLE_MESSAGES,
                            ChatTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(ChatTable.TABLE_MESSAGES,
                            ChatTable.COLUMN_ID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = database.getWritableDatabase();
        int rowsUpdated = 0;
        switch (uriType) {
            case CHATS:
                rowsUpdated = sqlDB.update(ChatTable.TABLE_MESSAGES,
                        values,
                        selection,
                        selectionArgs);
                break;
            case CHAT_ID:
                String id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = sqlDB.update(ChatTable.TABLE_MESSAGES,
                            values,
                            ChatTable.COLUMN_ID + "=" + id,
                            null);
                } else {
                    rowsUpdated = sqlDB.update(ChatTable.TABLE_MESSAGES,
                            values,
                            ChatTable.COLUMN_ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return rowsUpdated;
    }

    private void checkColumns(String[] projection) {
        String[] available = ChatTable.ALL_COLUMNS;
        if (projection != null) {
            HashSet<String> requestedColumns = new HashSet<String>(Arrays.asList(projection));
            HashSet<String> availableColumns = new HashSet<String>(Arrays.asList(available));
            // check if all columns which are requested are available
            if (!availableColumns.containsAll(requestedColumns)) {
                throw new IllegalArgumentException("Unknown columns in projection");
            }
        }
    }
}

