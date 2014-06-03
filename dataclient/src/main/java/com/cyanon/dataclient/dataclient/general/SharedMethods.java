package com.cyanon.dataclient.dataclient.general;

import android.content.Context;
import android.widget.Toast;

public class SharedMethods {

    //TODO: make this shit go away

    public static Runnable postToast(final String toast, final Context context)
    {
        Runnable runnable = new Runnable() {
            public void run() {
                Toast.makeText(context, toast, Toast.LENGTH_SHORT).show();
            }
        };

        return runnable;
    }
}
