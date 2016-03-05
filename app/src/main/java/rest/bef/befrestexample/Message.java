package rest.bef.befrestexample;

import org.json.JSONException;
import org.json.JSONObject;

import rest.bef.BefrestMessage;

/**
 * Created by hojjatimani on 3/2/2016 AD.
 */
public class Message {
    public static final int MSG = 1;
    public static final int SET_CONTACT = 2;
    public static final int SET_PRESENCE = 3;

    int type;
    String from;
    String msg;
    String timeSent;
    long timeReceived;

    public Message(BefrestMessage bMessage) {
        timeReceived = System.currentTimeMillis();
        timeSent = bMessage.getTimeStamp();
        JSONObject jsObject = null;
        try {
            jsObject = new JSONObject(bMessage.getData());
            switch (jsObject.getString("t")) {
                case "1":
                    type = MSG;
                    break;
                case "2":
                    type = SET_CONTACT;
                    break;
                case "3":
                    type = SET_PRESENCE;
                    break;
            }
            from = jsObject.getString("f");
            msg = jsObject.getString("m");
        } catch (JSONException e) {
            type = MSG;
            from = "unknown";
            msg = bMessage.getData();
            e.printStackTrace();
        }
    }

    public Message() {
    }
}
