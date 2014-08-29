package com.alteredworlds.buddyfied;

/**
 * Created by twcgilbert on 29/08/2014.
 */
public class Utils {
    public static Boolean isNullOrEmpty(String string) {
        if (null != string)
            return string.length() == 0;
        else
            return true;
    }
}
