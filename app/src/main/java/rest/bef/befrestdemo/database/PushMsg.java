package rest.bef.befrestdemo.database;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import rest.bef.BefrestMessage;
import rest.bef.befrestdemo.BefrestHelper;
import rest.bef.befrestdemo.NetworkHelper;
import rest.bef.befrestdemo.SignupHelper;
import rest.bef.befrestdemo.TimeHelper;
import rest.bef.befrestdemo.UIHelper;

/**
 * Created by hojjatimani on 1/24/2016 AD.
 */
public class PushMsg {
    public static final int MSG = 1;
    public static final int SET_CONTACT = 2;
    public static final int SET_PRESENCE = 3;

    public static final int FAILED = 0;
    public static final int SENDING = 1;
    public static final int SENT = 2;

    public int type;
    public String from;
    public String to;
    public String topic;
    public String msg;
    public long time;
    public int status;
    public boolean isIncoming;

    public int id;
    Uri uri;

    private PushMsg() {
    }

    public static PushMsg fromCursor(Cursor cursor) {
        PushMsg item = new PushMsg();
        item.type = MSG;
        item.id = cursor.getInt(0);
        item.from = cursor.getString(1);
        item.to = cursor.getString(2);
        item.topic = cursor.getString(3);
        item.msg = cursor.getString(4);
        item.time = cursor.getLong(5);
        item.status = cursor.getInt(6);
        item.isIncoming = cursor.getInt(7) == 0 ? false : true;
        return item;
    }

    public static PushMsg newTopicChat(Context context, String topic, String msg) {
        PushMsg item = new PushMsg();
        item.type = MSG;
        item.from = SignupHelper.getUserId(context);
        item.topic = topic;
        item.msg = msg;
        item.isIncoming = false;
        item.time = TimeHelper.now();
        return item;
    }

    public static PushMsg newMsgChat(Context context, String to, String msg) {
        PushMsg item = new PushMsg();
        item.type = MSG;
        item.from = SignupHelper.getUserId(context);
        item.to = to;
        item.msg = msg;
        item.isIncoming = false;
        item.time = TimeHelper.now();
        return item;
    }

    public static PushMsg newFromBefrestMsg(Context context, BefrestMessage rawMsg){
        PushMsg item = new PushMsg();
        item.time = System.currentTimeMillis();
        JSONObject jsObject = null;
        try {
            jsObject = new JSONObject(rawMsg.getData());
            switch (jsObject.getString("t")) {
                case "1":
                    item.type = MSG;
                    break;
                case "2":
                    item.type = SET_CONTACT;
                    break;
                case "3":
                    item.type = SET_PRESENCE;
                    break;
            }
            item.from = jsObject.getString("f");
            item.msg = jsObject.getString("m");
            item.time = jsObject.getLong("tm");
            if (jsObject.has("tp"))
                item.topic = jsObject.getString("tp");
            item.to = SignupHelper.getUserId(context);
        } catch (JSONException e) {
            item.type = MSG;
            item.from = "unknown";
            item.to = SignupHelper.getUserId(context);
            item.topic = "unknown";
            item.msg = rawMsg.getData();
            e.printStackTrace();
        }
        item.isIncoming = true;
        return item;
    }

    public static PushMsg newSetContactMsg(Context context, String contacts) {
        PushMsg item = new PushMsg();
        item.type = SET_CONTACT;
        item.msg = contacts;
        item.from = SignupHelper.getUserId(context);
        item.topic = "oddrun";
        item.time = System.currentTimeMillis();
        return item;
    }

    public static PushMsg newPresenceMsg(Context context) {
        PushMsg item = new PushMsg();
        item.type = SET_PRESENCE;
        item.from = SignupHelper.getUserId(context);
        item.msg = SignupHelper.getUserId(context);
        item.to = "admin";
        item.time = System.currentTimeMillis();
        return item;
    }

    public void send(Context context) {
        if (!NetworkHelper.isConnectedToInternet(context)) {
            UIHelper.notifyUser(context, NetworkHelper.NO_NOTWORK_MSG);
        } else {
            status = SENDING;
            updateInDbIfNeeded(context);
            new SendMessage(context).execute();
        }
    }

    public void resend(Context context){
        delete(context);
        save(context);
        send(context);
    }

    private class SendMessage extends AsyncTask<String, Void, String> {
        private static final String TAG = "SendMessage";
        Context context;

        SendMessage(Context context) {
            this.context = context;
            Log.d(TAG, "sending msg : " + msg + "  ,  to:" + to + "  ,  topic:" + topic);
        }

        @Override
        protected String doInBackground(String... params) {
            JSONObject jObj = new JSONObject();
            time = TimeHelper.now();
            try {
                jObj.put("t", "" + type);
                jObj.put("f", SignupHelper.getUserId(context));
                jObj.put("m", msg);
                jObj.put("tm", time + 1);
                if (topic != null) jObj.put("tp", topic);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String data = jObj.toString();
            String res;
            if (to != null)
                res = BefrestHelper.publishMessage(context, data, to);
            else
                res = BefrestHelper.publishTopicMessage(context, data, topic);
            return res;
        }

        @Override
        protected void onPostExecute(String s) {
            Log.d(TAG, "send result:" + s);
            if ("success".equals(s)) {
                Log.d(TAG, "success");
                status = SENT;
                updateInDbIfNeeded(context);
            } else {
                Log.d(TAG, "unSuccess");
                status = FAILED;
                updateInDbIfNeeded(context);
            }
        }
    }

    public void save(Context context) {
        Uri contentUri = ChatContentProvider.CONTENT_URI;
        ContentValues values = new ContentValues(7);
        values.put(ChatTable.COLUMN_FROM, from);
        values.put(ChatTable.COLUMN_TO, to);
        values.put(ChatTable.COLUMN_TOPIC, topic);
        values.put(ChatTable.COLUMN_MSG, msg);
        values.put(ChatTable.COLUMN_SENT, status);
        values.put(ChatTable.COLUMN_TIME, time);
        values.put(ChatTable.COLUMN_IS_INCOMING, isIncoming ? 1 : 0);
        uri = context.getContentResolver().insert(contentUri, values);
    }

    public void delete(Context context){
        if (uri == null)
            uri = ContentUris.withAppendedId(ChatContentProvider.CONTENT_URI, id);
        context.getContentResolver().delete(uri, null, null);
    }

    private void updateInDbIfNeeded(Context context) {
        if (type != MSG) return;
        if (uri == null)
            uri = ContentUris.withAppendedId(ChatContentProvider.CONTENT_URI, id);
        ContentValues values = new ContentValues(1);
        values.put(ChatTable.COLUMN_SENT, status);
        values.put(ChatTable.COLUMN_TIME, time);
        context.getContentResolver().update(uri, values, null, null);
    }
}