package rest.bef.befrestdemo;

import android.content.Context;

import rest.bef.Befrest;
import rest.bef.BefrestFactory;

import static rest.bef.befrestdemo.PrefrenceManager.PREF_NEEDS_SIGN_UP;
import static rest.bef.befrestdemo.PrefrenceManager.PREF_USER_ID;
import static rest.bef.befrestdemo.BefrestHelper.uId;

/**
 * Created by hojjatimani on 4/3/2016 AD.
 */
public class SignupHelper {

    public static void signUpAndStart(Context context, String chId, String team) {
        PrefrenceManager.saveBoolean(context, PREF_NEEDS_SIGN_UP, false);
        PrefrenceManager.saveString(context, PREF_USER_ID, chId);
        BefrestFactory.getInstance(context)
                .setCustomPushService(CustomPushService.class)
                .setLogLevel(Befrest.LOG_LEVEL_VERBOSE)
                .setChId(chId)
                .setUId(uId)
                .addTopic(team)
                .addTopic("oddrun")
                .setAuth(BefrestHelper.getSubscribeAuth(context, uId, chId))
                .start();
    }

    public static boolean needsSignUp(Context context) {
        return PrefrenceManager.getPrefs(context).getBoolean(PREF_NEEDS_SIGN_UP, true);
    }

    public static String getUserId(Context context) {
        return PrefrenceManager.getPrefs(context).getString(PREF_USER_ID, "NO_USER_ID");
    }
}
