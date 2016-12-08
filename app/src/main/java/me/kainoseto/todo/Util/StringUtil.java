package me.kainoseto.todo.Util;

import org.apache.commons.codec.binary.Base32;

/**
 * Created by TYLER on 12/6/2016.
 */

public class StringUtil {
    private static Base32 base32 = new Base32(true);

    /**
     * Creates a valid calendar item id according to api documentation: <a>https://developers.google.com/google-apps/calendar/v3/reference/events/delete</a>
     *
     * @param title -  Title of the calender item
     * @return
     */
    public static String formatCalendarItemId(String title){
        String ret =base32.encodeAsString(title.getBytes()).toLowerCase();
        String replaced = ret.replaceAll("=", "0");

        if(replaced.length() > 5){
            return replaced;
        }else{
            StringBuilder paddedResult = new StringBuilder(replaced);
            while (paddedResult.length() <= 5){
                paddedResult.append("0");
            }
            return paddedResult.toString();
        }
    }

    public static String catchNullString(String string){
        if (null != string){
            return string;
        }
        return "";
    }

    //TODO: Add method to generate description that contains subtasks

}
