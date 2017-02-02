package co.xelabs.todo.Util;

import com.google.api.client.util.DateTime;

/**
 * Created by TYLER on 12/8/2016.
 */

public class DateTimeUtil {

    //For avoiding npe
    public static DateTime safeParseRfc3339(String dateString){
        if(null != dateString && !dateString.isEmpty()){
            return DateTime.parseRfc3339(dateString);
        }
        return null;
    }

    //For avoiding npe
    public static String safeToStringRfc3339(DateTime dateTime){
        if(null != dateTime){
            return dateTime.toStringRfc3339();
        }
        return "";
    }

    /**
     * Returns a DateTime object with the current time. Mostly used for debugging
     * Does some other new stuff
     * @return
     */
    public static DateTime currTime(){
        return new DateTime(System.currentTimeMillis());
    }

    public static DateTime weekFromNow(){
        long now = System.currentTimeMillis();
        long oneWeekFromNow = now + 1000*60*60*24*7;
        return new DateTime(oneWeekFromNow);
    }
}
