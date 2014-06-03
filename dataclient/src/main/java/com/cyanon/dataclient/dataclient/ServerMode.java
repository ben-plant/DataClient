package com.cyanon.dataclient.dataclient;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.format.Time;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.cyanon.dataclient.dataclient.general.SharedMethods;
import com.cyanon.dataclient.dataclient.networking.DataClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class ServerMode extends Activity {

    //TODO: Make the app handle not being on Wifi more reasonably : currently throws NPE at L208

    private final static String LOG_HANDLE = "ServerMode";

    private static boolean SERVER_IS_RUNNING = false;
    private static boolean ACCEPTING_NEW_CLIENTS = false; //<------------------------ SWITCH CLIENT INTAKE ON OR OFF
    private static boolean READING_FROM_CLIENTS = false;  //<------------------------ SWITCH READING PORTS ON OR OFF

    private ServerSocket sSocket;
    private ArrayList<Socket> connectedClients;
    private ArrayList<BufferedReader> connectedInputStreams;

    private int numberOfConnectedClients = 0;

    private int PORT_NO;

    private NsdManager nsdManager;
    private NsdManager.RegistrationListener registrationListener;

    BufferedReader input;

    private Context context = this;

    final DecimalFormat dataFormat = new DecimalFormat();
    private Time time = new Time();
    private TextView gb;
    private TextView ts;

    public ServerMode() {
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        ServerSocketInit bootServerSocket = new ServerSocketInit();
        ClientConnector connectClient = new ClientConnector();
        PullFromSockets socketPuller = new PullFromSockets();
        bootServerSocket.execute();
        connectClient.execute();

        dataFormat.setMaximumFractionDigits(2);
        DataClientHandler dch = new DataClientHandler();
        getWindow().getDecorView().setSystemUiVisibility(  View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        setContentView(R.layout.main_server);

        socketPuller.execute();

        String timeFormat = new String("%d/%m/%Y - %H:%M:%S");
        time.set(System.currentTimeMillis() - SystemClock.elapsedRealtime());

        gb = (TextView)findViewById(R.id.textViewGB);
        ts = (TextView)findViewById(R.id.timeStamp);

        gb.setText(dataFormat.format(dch.getDataUsedMB()) + "MB"); //gb
        ts.setText(time.format(timeFormat)); //timestamp

        //Toast.makeText(context, "Server started successfully!", Toast.LENGTH_LONG).show();
    }

    protected void onPause()
    {
        super.onPause();
        READING_FROM_CLIENTS = false;
        ACCEPTING_NEW_CLIENTS = false;
        runOnUiThread(SharedMethods.postToast("Client interactions paused!", context));
    }

    protected void onResume() //<----------------- TODO: Check for dead clients
    {
        super.onResume();
        READING_FROM_CLIENTS = true;
        ACCEPTING_NEW_CLIENTS = true;
        runOnUiThread(SharedMethods.postToast("Client interactions resumed!", context));
    }

    public Runnable updateUIText(final String text) //<----------------- TODO: Address : Moved back from SharedMethods b/c dependency on updateText TO BE RESOLVED/DEPRECATED
    {
        Runnable runnable = new Runnable() {
            public void run() {
                updateText(text);
            }
        };

        return runnable;
    }

    private void updateText(String text)
    {
        gb.setText(text + "MB");
    }

    private class ServerSocketInit extends AsyncTask<Object, Void, Boolean>
    {
        protected Boolean doInBackground(Object... params)
        {
            boolean serverOnline = false;
            try {
                sSocket = new ServerSocket(55055); //set up server and advertise service || hardcoded for fv.01
                PORT_NO = sSocket.getLocalPort();

                WifiManager wm = (WifiManager)getSystemService(WIFI_SERVICE);
                WifiInfo wi = wm.getConnectionInfo();
                int ip_nf = wi.getIpAddress();

                //TODO: Fix the IP formatting
                String ip_f = (ip_nf & 0xFF ) + "." + ((ip_nf >> 8 ) & 0xFF) + "." +
                        ((ip_nf >> 16 ) & 0xFF) + "."   + (( ip_nf >> 24 ) & 0xFF) ;

                runOnUiThread(SharedMethods.postToast("Server mode engaged : " + ip_f, context));
                //registerNSDService();

                connectedClients = new ArrayList<Socket>();
                if (sSocket != null)
                {
                    ACCEPTING_NEW_CLIENTS = true;
                    serverOnline = true;
                }
                else
                {
                    runOnUiThread(SharedMethods.postToast("Couldn't open server socket! Cannot accept clients!", context));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return serverOnline;
        }

        protected void onPostExecute(Boolean serverRunning)
        {
            if (serverRunning)
            {
                SERVER_IS_RUNNING = true;
            }
        }
    }

    private class ClientConnector extends AsyncTask<Object, Void, Void> //<--------------------------------------------------- TODO: optimise for multiple clients
    {
        protected Void doInBackground(Object... params) {
            try {
                if (sSocket != null) {
                    if (!ACCEPTING_NEW_CLIENTS)
                    {
                        runOnUiThread(SharedMethods.postToast("This server is not currently accepting clients!", context));
                    }
                    else {
                        connectedInputStreams = new ArrayList<BufferedReader>();
                        while (ACCEPTING_NEW_CLIENTS) {
                            Socket incomingSocket = sSocket.accept();
                            runOnUiThread(SharedMethods.postToast("Client connected! Addr: " + incomingSocket.getInetAddress(), context));
                            BufferedReader input = new BufferedReader(new InputStreamReader(incomingSocket.getInputStream()));

                            connectedClients.add(numberOfConnectedClients, incomingSocket);
                            connectedInputStreams.add(numberOfConnectedClients, input);

                            numberOfConnectedClients++;
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class PullFromSockets extends AsyncTask<Object, Void, Void>
    {
        protected Void doInBackground(Object... params) {
            try {
                if (connectedInputStreams.size() > 0) {
                    while (READING_FROM_CLIENTS) {
                        float incomingText;
                        float dataUsed = 0;

                        for (BufferedReader reader : connectedInputStreams) {
                            incomingText = Float.parseFloat(reader.readLine());
                            runOnUiThread(updateUIText(dataFormat.format((dataUsed += incomingText) / 1024)));
                        }
                    }
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public void registerNSDService()
    {
        NsdServiceInfo nsdServiceInfo = new NsdServiceInfo();

        nsdServiceInfo.setServiceName(MainMenu.nsdServiceName);
        nsdServiceInfo.setServiceType("_http._tcp.");
        nsdServiceInfo.setPort(PORT_NO);

        nsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);
        nsdManager.registerService(nsdServiceInfo, NsdManager.PROTOCOL_DNS_SD, registrationListener);
    }

    public void initRegistrationListener()
    {
        registrationListener = new NsdManager.RegistrationListener()
        {
            public void onServiceRegistered(NsdServiceInfo nsdServiceInfo)
            {
                MainMenu.nsdServiceName = nsdServiceInfo.getServiceName();
            }

            public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int errorCode)
            {

            }

            public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo)
            {

            }

            public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int errorCode)
            {

            }
        };
    }
}
