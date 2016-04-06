package rest.bef.befrestdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by hojjatimani on 3/31/2016 AD.
 */
public class NetworkHelper {
    public static final String NO_NOTWORK_MSG = "دستگاه به اینترنت وصل نیست!";

    public static boolean isConnectedToInternet(Context context) {
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

    public static boolean checkNetworkAvailabilityAndNotifyUserIfNotAvailable(Context context) {
        if (isConnectedToInternet(context))
            return true;
        UIHelper.notifyUser(context, "no network!");
        return false;
    }
}
