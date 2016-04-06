package rest.bef.befrestdemo;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by hojjatimani on 3/31/2016 AD.
 */
public class UIHelper {

    public static void notifyUser(Context context, String msg){
        Toast.makeText(context , msg , Toast.LENGTH_SHORT).show();
    }
}
