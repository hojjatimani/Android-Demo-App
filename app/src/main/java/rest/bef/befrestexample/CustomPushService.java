package rest.bef.befrestexample;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import java.util.ArrayList;

import rest.bef.BefrestMessage;
import rest.bef.PushService;

/**
 * Created by hojjatimani on 2/28/2016 AD.
 */
public class CustomPushService extends PushService {
    private static final String TAG = "CustomPushService";

    @Override
    protected void onBefrestConnected() {
        super.onBefrestConnected();
        Log.d(TAG, "onBefrestConnected()");
        ApplicationLoader.sendPresence(this);
    }

    @Override
    protected void onPushReceived(ArrayList<BefrestMessage> messages) {
        MyDatabaseHelper db = new MyDatabaseHelper(this);
        boolean newMessage = false;
        for (BefrestMessage message : messages) {
            Message msg = new Message(message);
            if (msg.type == Message.MSG) {
                if (!ApplicationLoader.isJunkMessage(msg.msg)) {
                    newMessage = true;
                    db.insertMessage(msg);
                }
            } else if (msg.type == Message.SET_CONTACT) {
                ApplicationLoader.setContacts(this, msg.msg);
            } else {
                handlePresenceMethod(msg.msg);
            }
        }
        db.close();
        super.onPushReceived(messages);
        if (newMessage && !ApplicationLoader.dontShowNotif) showNotif();
    }

    @Override
    protected void onConnectionRefreshed() {
        super.onConnectionRefreshed();
        Log.d(TAG, "onConnectionRefreshed()");
    }

    private void handlePresenceMethod(String user) {
        String[] contacts = ApplicationLoader.getContacts(this);
        boolean isNewUser = true;
        for (String contact : contacts) {
            if (user.equals(contact)) isNewUser = false;
        }
        if (isNewUser) {
            String newContactList = "";
            for (String contact : contacts) {
                newContactList += contact + "@";
            }
            newContactList += user;
            ApplicationLoader.setContacts(this, newContactList);
            ApplicationLoader.sendSetContactMessage(this, newContactList);
        }
    }


    private void showNotif() {
        Intent intent = new Intent(this, ActivityMain.class);

        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("Befrest Test")
                .setContentTitle("Befrest Test")
                .setContentText("You have new push messages!")
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 300, 50, 300});
        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(1, builder.build());
    }
}
