package rest.bef.befrestdemo;

import android.content.Context;
import static rest.bef.befrestdemo.PrefrenceManager.*;

/**
 * Created by hojjatimani on 4/3/2016 AD.
 */
public class UserPrefrences {

    public static int getTopicMsgTTL(Context context){
        return PrefrenceManager.getPrefs(context).getInt(PREF_TOPIC_MSG_TTL , 0);
    }

    public static int getMessageTTL(Context context){
        return PrefrenceManager.getPrefs(context).getInt(PREF_REGULAR_MSG_TTL , 86400);
    }

    public static void setTopicTTL(Context context, int value){
        PrefrenceManager.saveInt(context, PREF_TOPIC_MSG_TTL, value);
    }

    public static void setRegularTTL(Context context, int value){
        PrefrenceManager.saveInt(context, PREF_REGULAR_MSG_TTL, value);
    }
}
