package rest.bef.befrestdemo;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by hojjatimani on 4/5/2016 AD.
 */
public class TimeHelper {
    static SimpleDateFormat formatter = new SimpleDateFormat("MM/dd HH:mm");
    public static long now(){
        return System.currentTimeMillis();
    }

    public static final String shortTimeStamp(long time){
        return formatter.format(new Date(time));
    }
}
