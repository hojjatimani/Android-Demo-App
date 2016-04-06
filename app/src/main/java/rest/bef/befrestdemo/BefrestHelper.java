package rest.bef.befrestdemo;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

import rest.bef.Befrest;
import rest.bef.BefrestFactory;

/**
 * Created by hojjatimani on 4/3/2016 AD.
 */
public class BefrestHelper {
    private static final String TAG = "BefrestHelper";
    static final long uId = 10099; //uid       befrestdemo
    private static final String SHARED_KEY = "jdksjfkdljjdksjfkdljjdksjfkdljdsflskjdf";
    private static final String API_KEY = "AAB58F907999961708595B21AFD1ED10";

    public static String publishMessage(Context context, String data, String toChId) {
        try {
            URL url = new URL(getPublishUrl(toChId));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "close");
            conn.addRequestProperty("X-BF-AUTH", getPublishAuth(toChId));
            conn.addRequestProperty("X-BF-TEXP", "" + UserPrefrences.getMessageTTL(context));
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(5 * 1000);
            conn.connect();
            //wait to connect
            conn.getOutputStream().write(data.getBytes());
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            Log.d(TAG, "publish status: " + conn.getResponseCode() + " " + conn.getResponseMessage());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String res = "";
            String line;
            while ((line = bufferedReader.readLine()) != null)
                res += line;
            Log.d(TAG, "publish res : " + res);
            conn.disconnect();
            return "success";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String publishTopicMessage(Context context, String data, String topic) {
        try {
            URL url = new URL(getTopicPublishUrl(topic));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("connection", "close");
            conn.addRequestProperty("X-BF-AUTH", getTopicPublicAuth(topic));
            conn.addRequestProperty("X-BF-TEXP", "" + UserPrefrences.getTopicMsgTTL(context));
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(5 * 1000);
            conn.connect();
            //wait to connect
            conn.getOutputStream().write(data.getBytes());
            conn.getOutputStream().flush();
            conn.getOutputStream().close();
            Log.d(TAG, "SendMessageStatus: " + conn.getResponseCode() + " " + conn.getResponseMessage());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String res = "";
            String line;
            while ((line = bufferedReader.readLine()) != null)
                res += line;
            Log.d(TAG, "SendMessageResult : " + res);
            conn.disconnect();
            return "success";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public static String getUserStatus(Context context, String userId) {
        try {
            URL url = new URL(getChannelStatusUrl(userId));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("connection", "close");
            conn.addRequestProperty("X-BF-AUTH", getChannelStatusCheckAuth(userId));
            conn.setConnectTimeout(5 * 1000);
            conn.setReadTimeout(5 * 1000);
            conn.connect();
            Log.d(TAG, "getChannelStatusResponse: " + conn.getResponseCode() + " " + conn.getResponseMessage());
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String res = "";
            String line;
            while ((line = bufferedReader.readLine()) != null)
                res += line;
            conn.disconnect();
            Log.d(TAG, "getChannelStatusResult : " + res);
            //parsing result
            JSONObject jsObject = new JSONObject(res);
            JSONObject entity = jsObject.getJSONObject("entity");
            int subscribers = entity.getInt("subscribers");
            if (subscribers > 0)
                return ActivityPvChat.STATUS_ONLINE;
            else
                return ActivityPvChat.STATUS_OFFLINE;
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ActivityPvChat.STATUS_UNKNOWN;
    }

    public static String getPublishUrl(String to) {
        return String.format(Locale.US, "https://gw.bef.rest/xapi/%d/publish/%d/%s", 1, uId, to);
    }

    public static String getTopicPublishUrl(String topic) {
        return String.format(Locale.US, "https://gw.bef.rest/xapi/%d/t-publish/%d/%s", 1, uId, topic);
    }

    public static String getChannelStatusUrl(String chId) {
        return String.format(Locale.US, "https://gw.bef.rest:8443/xapi/%d/channel-status/%d/%s", 1, uId, chId);
    }

    public static String getSubscribeAuth(Context context, long uId, String chId) {
        Befrest befrest = BefrestFactory.getInstance(context);
        return sign(String.format(Locale.US, "/xapi/%d/subscribe/%d/%s/%d", 1, uId, chId, befrest.getSdkVersion()));
    }

    private static String getPublishAuth(String toChId) {
        return sign(String.format(Locale.US, "/xapi/%d/publish/%d/%s", 1, uId, toChId));
    }

    private static String getTopicPublicAuth(String topic) {
        return sign(String.format(Locale.US, "/xapi/%d/t-publish/%d/%s", 1, uId, topic));
    }

    private static String getChannelStatusCheckAuth(String chId) {
        return sign(String.format(Locale.US, "/xapi/%d/channel-status/%d/%s", 1, uId, chId));
    }

    private static String sign(String parameter2) {
        MessageDigest dig = null;
        try {
            dig = MessageDigest.getInstance("md5");
        } catch (Exception e) {
            e.printStackTrace();
        }
        String payload = String.format("%s,%s", SHARED_KEY, generateTokenInYourServer(parameter2));

        dig.reset();
        dig.update(payload.getBytes());
        byte[] digest = dig.digest();

        String b64 = Base64.encodeToString(digest, Base64.DEFAULT);
        b64 = b64.replace("+", "-").replace("=", "").replace("/", "_").replace("\n", "");
        Log.d("SIGN", b64);
        return b64;
    }

    private static String generateTokenInYourServer(String parameter2) {
        MessageDigest dig = null;
        try {
            dig = MessageDigest.getInstance("md5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        String payload = String.format("%s,%s", API_KEY, parameter2);
        dig.reset();
        dig.update(payload.getBytes());
        byte[] digest = dig.digest();

        String b64 = Base64.encodeToString(digest, Base64.DEFAULT);
        b64 = b64.replace("+", "-").replace("=", "").replace("/", "_").replace("\n", "");
        return b64;
    }
}
