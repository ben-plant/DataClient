package com.cyanon.dataclient.dataclient.general;

import android.os.SystemClock;
import android.util.Log;

public class DateTimeHandler {

    //TODO: deprecate and delete as appropriate

    public DateTimeHandler()
    {

    }

    public static String getBootTimeStamp()
    {
        long seconds = ((System.currentTimeMillis() - SystemClock.elapsedRealtime()) / 1000);
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours   / 24;

        Log.v(DateTimeHandler.class.getSimpleName(), String.format("%1$02d days ago at %2$02d:%3$02d", seconds, hours % 24, minutes % 60));
        return String.format("%1$02d days ago at %2$02d:%3$02d", days, hours % 24, minutes % 60);
    }

}
