package com.cyanon.dataclient.dataclient;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainMenu extends Activity {

    public static String nsdServiceName = "DataClient";


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_launch);

        final Button buttonData = (Button)findViewById(R.id.isDataCounter);
        final Button buttonDemo = (Button)findViewById(R.id.isDemoHandset);

        final Intent dataIntent = new Intent(this, ServerMode.class);
        final Intent demoIntent_DEBUG = new Intent(this, ClientMode.class);

        buttonData.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                startActivity(dataIntent); //REVISION: ServerMode now launches Activity
            }
        });

        buttonDemo.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v)
            {
                startActivity(demoIntent_DEBUG); //debug activity to send spoof data
            }
        });
    }
}
