package com.cyanon.dataclient.dataclient.general;

import android.os.SystemClock;

public class DateTimeHandler {

    //TODO: deprecate and delete as appropriate

    public DateTimeHandler()
    {

    }

    public long getSystemTime()
    {
        return SystemClock.elapsedRealtime();
    }

    public String getBootTimestamp()
    {
        long seconds = ((this.getSystemTime() + 500) / 1000);
        long minutes = seconds / 60;
        long hours   = minutes / 60;
        long days    = hours   / 24;

        return String.format("%1$02d days, %2$02d hours, %3$02d minutes, %4$02d seconds", days, hours % 24, minutes % 60, seconds % 60);
    }

}
