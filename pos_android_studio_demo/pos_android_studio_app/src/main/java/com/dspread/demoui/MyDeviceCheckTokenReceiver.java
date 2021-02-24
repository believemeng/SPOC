package com.dspread.demoui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dspread.demoui.services.SyncDeviceTokenService;

public class MyDeviceCheckTokenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        Intent i = new Intent(context, SyncDeviceTokenService.class);
        context.startService(i);
    }
}
