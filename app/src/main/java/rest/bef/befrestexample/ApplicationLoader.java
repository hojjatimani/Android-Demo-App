/******************************************************************************
 * Copyright 2015-2016 BefrestImpl
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package rest.bef.befrestexample;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
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

public class ApplicationLoader extends Application {
//    static final long uId = 2; //uid

//    private static final String SHARED_KEY = "23e78b4b-079b-4556-aad0-beded33ed064";
//    private static final String API_KEY = "e2a29f25-2a38-4cac-bbc5-1cec1a02fba0";

    static final long uId = 10086; //uid
    private static final String SHARED_KEY = "qwertyuiopasdfghjklzxcvbnm1234567890qweasdzx";
    private static final String API_KEY = "108A56A5B65E36C860D1BF1E5447E479";


    public static final String PREFS = "PREFS";
    public static final String PREF_NEEDS_SIGN_UP = "PREF_NEEDS_SIGN_UP";
    public static final String PREF_USER_ID = "PREF_USER_ID";
    public static final String PREF_CONTACTS = "PREF_CONTACTS";

    static boolean dontShowNotif = false;

    private final String TAG = getClass().getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        if (!needsSignUp(this))
            BefrestFactory.getInstance(this).start();
    }

    public static void signUpAndStart(Context context, String chId, String team) {
        SharedPreferences.Editor editor = context.getSharedPreferences(PREFS, MODE_PRIVATE).edit();
        editor.putBoolean(PREF_NEEDS_SIGN_UP, false);
        editor.putString(PREF_USER_ID, chId);
        editor.commit();
        BefrestFactory.getInstance(context)
                .advancedSetCustomPushService(CustomPushService.class)
                .setLogLevel(Befrest.LOG_LEVEL_VERBOSE)
                .setChId(chId)
                .setUId(uId)
                .addTopic(team)
                .addTopic("oddrun")
                .setAuth(sign(String.format(Locale.US, "/xapi/%d/subscribe/%d/%s/%d", 1, uId, chId, 1)))
                .start();
    }

    public static boolean needsSignUp(Context context) {
        return context.getSharedPreferences(PREFS, MODE_PRIVATE).getBoolean(PREF_NEEDS_SIGN_UP, true);
    }

    public static String getUserId(Context context) {
        return context.getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_USER_ID, "NO_USER_ID");
    }

    public static void setContacts(Context context, String contacts) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, MODE_PRIVATE);
        String current = prefs.getString(PREF_CONTACTS, "");
        if (contacts.length() > current.length())
            prefs.edit().putString(PREF_CONTACTS, contacts).commit();
    }


    public static String[] getContacts(Context context) {
        String contacts = context.getSharedPreferences(PREFS, MODE_PRIVATE).getString(PREF_CONTACTS, "");
        if (contacts.length() > 0) return contacts.split("@");
        return new String[0];
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

    public static void sendMessage(Context context, String msg, String to, String topic) {
        if (!isConnectedToInternet(context))
            Toast.makeText(context, "No Network!", Toast.LENGTH_SHORT).show();
        else {
            JSONObject jObj = new JSONObject();
            try {
                jObj.put("t", "" + Message.MSG);
                jObj.put("f", getUserId(context));
                jObj.put("m", msg);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            new SendMessage(context, jObj.toString(), to, topic).execute();
        }
    }

    public static void sendPresence(Context context) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("t", "" + Message.SET_PRESENCE);
            jObj.put("f", getUserId(context));
            jObj.put("m", getUserId(context));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendMessage(context, jObj.toString(), "admin", null).execute();
    }

    public static void sendSetContactMessage(Context context, String contactsList) {
        JSONObject jObj = new JSONObject();
        try {
            jObj.put("t", "" + Message.SET_CONTACT);
            jObj.put("f", getUserId(context));
            jObj.put("m", contactsList);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        new SendMessage(context, jObj.toString(), null, "oddrun").execute();
    }

    private static class SendMessage extends AsyncTask<String, Void, String> {
        public final String TAG = SendMessage.class.getSimpleName();
        private String data;
        private String topic;
        private String to;
        Context context;

        SendMessage(Context context, String data, String to, String topic) {
            this.data = data;
            this.context = context;
            this.to = to;
            this.topic = topic;
            Log.d(TAG, "sending msg : " + data + "  ,  " + to + "  ,  " + topic);
        }

        @Override
        protected String doInBackground(String... params) {
            try {
                String s;
                if (topic == null)
                    s = getPublishUrl(to);
                else
                    s = getTopicPublishUrl(topic);
                URL url = new URL(s);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("connection", "close");
                if (topic == null) {
                    conn.addRequestProperty("X-BF-AUTH", sign(String.format(Locale.US, "/xapi/%d/publish/%d/%s", 1, uId, to)));
                } else {
                    conn.addRequestProperty("X-BF-AUTH", sign(String.format(Locale.US, "/xapi/%d/t-publish/%d/%s", 1, uId, topic)));
                    conn.addRequestProperty("X-BF-TEXP", "604800");
                }
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

        @Override
        protected void onPostExecute(String s) {
            if ("success".equals(s)) {
                if (to != null) {
                    Toast.makeText(context, "msg successfully pushed to @" + to, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(context, "msg successfully published on topic @" + topic, Toast.LENGTH_SHORT).show();
            } else {
                if (to != null) {
                    Toast.makeText(context, "could not push msg to @" + to, Toast.LENGTH_SHORT).show();
                } else
                    Toast.makeText(context, "could not publish msg on topic @" + topic, Toast.LENGTH_SHORT).show();
            }
        }


        static String getPublishUrl(String to) {
            return String.format(Locale.US, "https://gw.bef.rest/xapi/%d/publish/%d/%s", 1, uId, to);
        }

        static String getTopicPublishUrl(String topic) {
            return String.format(Locale.US, "https://gw.bef.rest/xapi/%d/t-publish/%d/%s", 1, uId, topic);
        }
    }

    static boolean isConnectedToInternet(Context context) {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return true;
            }

            netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
        return false;
    }

    static void showToast(Context context, String text) {
        Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
    }

    static boolean isJunkMessage(String msg) {
        if ("all djg".equals(msg) || "all HD hfjfi".equals(msg) || "all jffuf".equals(msg))
            return true;
        return false;
    }
}