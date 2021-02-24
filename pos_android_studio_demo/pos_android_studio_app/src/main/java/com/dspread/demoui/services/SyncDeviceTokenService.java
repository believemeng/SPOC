package com.dspread.demoui.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
//import android.support.annotation.Nullable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.dspread.demoui.AlarmReceiver;
import com.dspread.demoui.MyDeviceCheckTokenReceiver;
import com.dspread.demoui.beans.DeviceTokenStatusEvent;
import com.dspread.demoui.utils.TRACE;

import org.greenrobot.eventbus.EventBus;

import java.security.SecureRandom;
import java.util.Random;

/**
 * Time:2020/9/4
 * Author:Qianmeng Chen
 * Description:sync the devices token with the mpos and backend
 */
public class SyncDeviceTokenService extends Service {
    private Context context;
    private final Random mRandom = new SecureRandom();
    private AlarmManager manager;
    private PendingIntent pi;
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                TRACE.w("test token  success");
                requestDigital();
            }
        }
    };


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TRACE.w("start detect the token service");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);
            }
        }).start();
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 5*60*1000;
        Log.e("bai","Time:"+time);
        long triggerAtTime = SystemClock.elapsedRealtime()+(time);
        Intent i = new Intent(this, MyDeviceCheckTokenReceiver.class);
        pi = PendingIntent.getBroadcast(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void finishService(){
        EventBus.getDefault().post(new DeviceTokenStatusEvent(false));
        manager.cancel(pi);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        finishService();
    }

    private void requestDigital() {
        //从服务端获取数字信封，然后调用接口去灌数字信封即可 pos.udpateWorkKey(digEnvelopStr);
        EventBus.getDefault().post(new DeviceTokenStatusEvent(true));
    }
}
