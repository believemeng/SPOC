package com.dspread.demoui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dspread.demoui.services.SyncDeviceStateService;

/**
 * Time:2020/9/4
 * Author:Qianmeng Chen
 * Description:
 */
public class AlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, SyncDeviceStateService.class);
        context.startService(i);
    }
}
