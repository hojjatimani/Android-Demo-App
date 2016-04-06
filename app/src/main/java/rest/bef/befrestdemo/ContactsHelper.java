package rest.bef.befrestdemo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import rest.bef.befrestdemo.database.PushMsg;

import static rest.bef.befrestdemo.PrefrenceManager.PREF_CONTACTS;

/**
 * Created by hojjatimani on 3/31/2016 AD.
 */
public class ContactsHelper {

    static String[] getContactsList(Context context) {
        String contacts = PrefrenceManager.getPrefs(context).getString(PREF_CONTACTS, "");
        if (contacts.length() > 0) return contacts.split("@");
        return new String[0];
    }

    static void setContacts(Context context, String contacts) {
        String current = PrefrenceManager.getPrefs(context).getString(PREF_CONTACTS, "");
        if (contacts.length() > current.length()) {
            PrefrenceManager.saveString(context, PREF_CONTACTS, contacts);
        }
    }

    static void handlePresenceMessage(Context context, String user) {
        String[] contacts = getContactsList(context);
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
            setContacts(context, newContactList);
            PushMsg item = PushMsg.newSetContactMsg(context, newContactList);
            item.send(context);
        }
    }

    public static void sendPresenceIfNeeded(Context context) {
        String[] contactsList = getContactsList(context);
        String myId = SignupHelper.getUserId(context);
        boolean found = false;
        for (String s : contactsList) {
            if (s.equals(myId))
                found = true;
        }
        if (!found) sendPresence(context);
    }

    private static void sendPresence(Context context) {
        PushMsg msg = PushMsg.newPresenceMsg(context);
        msg.send(context);
    }
}