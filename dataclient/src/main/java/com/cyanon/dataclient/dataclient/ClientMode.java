package com.cyanon.dataclient.dataclient;

import android.app.Activity;
import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.cyanon.dataclient.dataclient.general.SharedMethods;
import com.cyanon.dataclient.dataclient.networking.DataClientHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;


public class ClientMode extends Activity {

    private final static String LOG_HANDLE = "ServerMode";

    private Socket outgoingSocket;

    private NsdManager.DiscoveryListener discoveryListener;

    BufferedReader input;
    PrintWriter output;

    private final int PORT_NO = 55055; //hardcoded until network discovery implemented

    private Context context = this;

    private EditText ipField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_client);

        Button fireBootCommand = (Button)findViewById(R.id.connectButton);
        ipField = (EditText)findViewById(R.id.ipAddrField);

        fireBootCommand.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                new Thread()
                {
                    public void run()
                    {
                        try
                        {
                            bootClient();
                        }
                        catch (Exception e)
                        {
                            e.printStackTrace();
                        }
                    }
                }.start();
            }
        });
    }


    public void bootClient() {
        runOnUiThread(SharedMethods.postToast("Client mode engaged...", context)); //<------------------------- move postToast to a shared import
        new Thread() {
            public void run() {
                try {
                    outgoingSocket = new Socket(ipField.getText().toString(), PORT_NO); //<-------------- fix this hardcode
                    output = new PrintWriter(outgoingSocket.getOutputStream());
                    input = new BufferedReader(new InputStreamReader(System.in));
                    output.flush();
                    Log.d(LOG_HANDLE, "Connected to server!");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if (outgoingSocket != null) {

                    boolean running = true;
                    float oldData = 0;

                    while (running && outgoingSocket.isConnected()) //TODO: test this!!!!!!!!!!!!!!!!!!!!!!!! <-----------------------------------------
                    {
                        long lastSeconds = 0;
                        long seconds = System.currentTimeMillis() / 1000;
                        if (seconds != lastSeconds) { //which it wont
                            float currData = DataClientHandler.getDataUsedKB();
                            float diffData = currData - oldData;
                            Log.v(LOG_HANDLE, "Writing " + diffData + " to socket...");
                            output.println(diffData);
                            output.flush();

                            oldData = currData;
                        }
                        seconds = lastSeconds;
                    }
                    if (!outgoingSocket.isConnected())
                    {
                        Toast.makeText(context, "The server has disconnected!", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    SharedMethods.postToast("ERROR!", context);
                    Log.v(LOG_HANDLE, "ERROR!");
                }
            }
        }.start();
    }

    public void initDiscoveryListener() {
        discoveryListener = new NsdManager.DiscoveryListener() {

            public void onDiscoveryStarted(String regType) {

            }

            public void onServiceFound(NsdServiceInfo nsdServiceInfo) {

            }

            public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
                Log.e(LOG_HANDLE, "Service lost! : " + nsdServiceInfo);
            }

            public void onDiscoveryStopped(String serviceType) {
                Log.i(LOG_HANDLE, "Discovery of " + serviceType + " stopped!");
            }

            public void onStartDiscoveryFailed(String serviceType, int errorCode)
            {

            }

            public void onStopDiscoveryFailed(String serviceType, int errorCode)
            {

            }
        };
    }
}
