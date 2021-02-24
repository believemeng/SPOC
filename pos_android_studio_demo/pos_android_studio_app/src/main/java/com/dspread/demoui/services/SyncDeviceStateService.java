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
//import android.support.annotation.NonNull;
//import android.support.annotation.Nullable;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dspread.demoui.AlarmReceiver;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.apis.GetDeviceVerifyResultAPI;
import com.dspread.demoui.net.apis.GetNonceAPI;
import com.dspread.demoui.utils.CheckCvmAppStateUtil;
import com.dspread.demoui.utils.CheckDeviceRuntimeStatusUtil;
import com.dspread.demoui.utils.CommonUtils;
import com.dspread.demoui.utils.TRACE;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.safetynet.SafetyNet;
import com.google.android.gms.safetynet.SafetyNetApi;
import com.google.android.gms.safetynet.SafetyNetClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

/**
 * Time:2020/9/4
 * Author:Qianmeng Chen
 * Description:sync the device state if is rooted to unsafe
 * */
public class SyncDeviceStateService extends Service {
    private Context context;
    private final Random mRandom = new SecureRandom();
    private AlarmManager manager;
    private PendingIntent pi;
    private String nonce;
    private byte[] requestNonce;
    private long requestTimestamp;
    private String packageName;
    private List<String> apkCertificateDigests;
    private static int MAX_TIMESTAMP_DURATION = 2 * 60 * 1000;
    private String apiKey = "AIzaSyA8lst29gmD-Sq76SAz0dn1VRmiYHQdHmc";
    private Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(msg.what == 1){
                TRACE.w("test success");
                checkSafetyNet();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
//            startForeground(1,new Notification());
//        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                mHandler.sendEmptyMessage(1);
            }
        }).start();
        manager = (AlarmManager) getSystemService(ALARM_SERVICE);
        int time = 30*60*1000;
        Log.e("bai","Time:"+time);
        long triggerAtTime = SystemClock.elapsedRealtime()+(time);
        Intent i = new Intent(this, AlarmReceiver.class);
        pi = PendingIntent.getBroadcast(this,0,i,0);
        manager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,triggerAtTime,pi);
        return super.onStartCommand(intent, flags, startId);
    }

    private void finishService(){
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

    private void checkSafetyNet(){
        this.context = getApplicationContext();
        boolean rootedStatus = CheckDeviceRuntimeStatusUtil.isRooted();
        if(rootedStatus){
            finishService();
            return;
        }
        if (GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this) ==
                ConnectionResult.SUCCESS) {
            // The SafetyNet Attestation API is available.
            getNonce();
//                sendSafetyNetRequest("c34d7533621c4a4aa9cc0b9791a3453826db84cba31cb968");
        } else {
            // Prompt user to update Google Play Services.
            Toast.makeText(this,"Please update the Google Play Services",Toast.LENGTH_LONG).show();
            return;
        }
    }

    private void getNonce(){
        GetNonceAPI.newInstance(context, new RetrofitCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Object result) {
                nonce = (String) result;
                sendSafetyNetRequest((String) result);
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {

            }
        }).request();
    }

    private void sendSafetyNetRequest(String nonceData){
        requestNonce = getRequestNonce(nonceData);
        requestTimestamp = System.currentTimeMillis();
        packageName = this.getPackageName();
        apkCertificateDigests = CommonUtils.calcApkCertificateDigests(this,packageName);
        SafetyNetClient client = SafetyNet.getClient(this);
        Task<SafetyNetApi.AttestationResponse> task = client.attest(requestNonce, apiKey);
        task.addOnSuccessListener( mSuccessListener).addOnFailureListener( mFailureListener);
    }

    private byte[] getRequestNonce(String data) {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        byte[] bytes = new byte[32];
        mRandom.nextBytes(bytes);
        try {
            byteStream.write(bytes);
            byteStream.write(data.getBytes());
        } catch (IOException e) {
            return null;
        }

        return byteStream.toByteArray();
    }

    private OnSuccessListener<SafetyNetApi.AttestationResponse> mSuccessListener =
            new OnSuccessListener<SafetyNetApi.AttestationResponse>() {
                @Override
                public void onSuccess(SafetyNetApi.AttestationResponse attestationResponse) {
                    /*
                     Successfully communicated with SafetyNet API.
                     Use result.getJwsResult() to get the signed result data. See the server
                     component of this sample for details on how to verify and parse this result.
                     */
                    String mResult = attestationResponse.getJwsResult();
                    TRACE.d("success check "+mResult);
                    getVerifyResult(mResult);
//                    SafetyNetResponse response = parseJsonWebSignature(mResult);
//                    if(!response.isCtsProfileMatch() || !response.isBasicIntegrity()){
//                        finishService();
//                        return;
//                    }else{
//                        if (validateSafetyNetResponsePayload(response)) {
//
//                        }
//                    }

                        /*
                         TODO(developer): Forward this result to your server together with
                         the nonce for verification.
                         You can also parse the JwsResult locally to confirm that the API
                         returned a response by checking for an 'error' field first and before
                         retrying the request with an exponential backoff.

                         NOTE: Do NOT rely on a local, client-side only check for security, you
                         must verify the response on a remote server!
                        */
                }
            };

    /**
     * Called when an error occurred when communicating with the SafetyNet API.
     */
    private OnFailureListener mFailureListener = new OnFailureListener() {
        @Override
        public void onFailure(@NonNull Exception e) {
            // An error occurred while communicating with the service.

            if (e instanceof ApiException) {
                // An error with the Google Play Services API contains some additional details.
                ApiException apiException = (ApiException) e;
                TRACE.d( "Error: " +
                        CommonStatusCodes.getStatusCodeString(apiException.getStatusCode()) + ": " +
                        apiException.getStatusMessage());
            } else {
                // A different, unknown type of error occurred.
                TRACE.d("ERROR! " + e.getMessage());
            }
            Toast.makeText(context,"Request google service is failed!",Toast.LENGTH_LONG).show();
        }
    };

    private void getVerifyResult(String jws){
        GetDeviceVerifyResultAPI.newInstance(context, nonce, jws, new RetrofitCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {
                Toast.makeText(context,"Your device is unsafe, will exit the app !",Toast.LENGTH_LONG).show();
                CheckCvmAppStateUtil.killAppProcess(context);
            }
        }).request();
    }

}
