package rest.bef.befrestdemo;

import android.content.Context;

import rest.bef.BefrestFactory;
import rest.bef.BefrestMessage;
import rest.bef.BefrestPushReceiver;

/**
 * Created by hojjatimani on 5/10/2016 AD.
 */
public class AuthProblemReceiver extends BefrestPushReceiver {
    @Override
    public void onPushReceived(Context context, BefrestMessage[] messages) {

    }

    @Override
    public void onAuthorizeProblem(Context context) {
        String auth = BefrestHelper.getSubscribeAuth(context, BefrestHelper.uId, SignupHelper.getUserId(context));
        BefrestFactory.getInstance(context).setAuth(auth).start();
    }
}
