package com.cyanon.dataclient.dataclient.networking;

import android.net.TrafficStats;

import java.lang.*;

public class DataClientHandler {

    //TODO: Change to formatting class?

    public DataClientHandler()
    {
    }

    private static Float getDataUsed()
    {
        return (float)TrafficStats.getTotalRxBytes();
    }

    public static Float getDataUsedKB()
    {
        Float kbytes = (getDataUsed() / 1024);
        return kbytes;
    }

    public static Float getDataUsedMB()
    {
        Float mbytes = (getDataUsedKB() / 1024);
        return mbytes;
    }

    //TODO: work out of build
    public static Float getDataUsedGB() //returns 0 if less than 1
    {
        Float gbytes = (getDataUsedMB() / 1024);
        return gbytes;
    }
}
