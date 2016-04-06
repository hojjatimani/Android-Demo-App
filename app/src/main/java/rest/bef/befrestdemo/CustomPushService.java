package rest.bef.befrestdemo;

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
import rest.bef.befrestdemo.database.PushMsg;

/**
 * Created by hojjatimani on 2/28/2016 AD.
 */
public class CustomPushService extends PushService {
    private static final String TAG = "CustomPushService";
    int lastMsgType;

    @Override
    protected void onBefrestConnected() {
        super.onBefrestConnected();
        Log.d(TAG, "onBefrestConnected()");
        ContactsHelper.sendPresenceIfNeeded(this);
    }

    @Override
    protected void onPushReceived(ArrayList<BefrestMessage> messages) {
        boolean newMsg = false;
        for (BefrestMessage message : messages) {
            PushMsg item = PushMsg.newFromBefrestMsg(this, message);
            if (item.type == PushMsg.MSG) {
                item.save(this);
                boolean isTopic = item.topic != null;
                String from = item.topic != null ? item.topic : item.from;
                String notifIsFor = isTopic ? ApplicationLoader.TOPIC : from;
                if (ApplicationLoader.shouldFireNotifFor(notifIsFor))
                    showNotif(isTopic, from);
            } else if (item.type == PushMsg.SET_CONTACT) {
                ContactsHelper.setContacts(this, item.msg);
            } else {
                ContactsHelper.handlePresenceMessage(this, item.msg);
            }
        }
        super.onPushReceived(messages);
    }

    @Override
    protected void onConnectionRefreshed() {
        super.onConnectionRefreshed();
        Log.d(TAG, "onConnectionRefreshed()");
    }


    private void showNotif(boolean isTopic, String from) {
        Intent intent;
        String title;
        String msg;
        if (isTopic) {
            intent = new Intent(this, ActivityMain.class);
            title = "پیام تاپیک جدید!";
            msg = "پیام جدید در تاپیک " + from;
        } else {
            intent = new Intent(this, ActivityPvChat.class);
            intent.putExtra(ActivityPvChat.USER_ID_KEY, from);
            title = "پیام جدید!";
            msg = "پیام جدید از " + from;
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        // Open NotificationView.java Activity
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker(getResources().getString(R.string.app_name))
                .setContentTitle(title)
                .setContentText(msg)
                .setAutoCancel(true)
                .setContentIntent(pIntent)
                .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
                .setVibrate(new long[]{0, 300, 50, 300});
        // Create Notification Manager
        NotificationManager notificationmanager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        // Build Notification with Notification Manager
        notificationmanager.notify(from.hashCode(), builder.build());
    }
}