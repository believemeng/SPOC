package com.dspread.demoui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;


import androidx.appcompat.graphics.drawable.DrawerArrowDrawable;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.dspread.august.common.wbaes.AES;
import com.dspread.august.common.wbaes.WhiteBoxAESUtil;
import com.dspread.demoui.R;
import com.dspread.demoui.beans.ConstantsBean;
import com.dspread.demoui.beans.DeviceTokenStatusEvent;
import com.dspread.demoui.beans.Transactions;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.apis.GetAESTableAPI;
import com.dspread.demoui.net.apis.GetDevicesVerifyTokenAPI;

import com.dspread.demoui.net.apis.LogoutAPI;
import com.dspread.demoui.net.apis.UploadTransactionAPI;
import com.dspread.demoui.services.SyncDeviceTokenService;
import com.dspread.demoui.utils.CheckCvmAppStateUtil;
import com.dspread.demoui.utils.CheckDeviceRuntimeStatusUtil;
import com.dspread.demoui.utils.ClearDataUtils;
import com.dspread.demoui.utils.CustomPinKeyboardDialog;
import com.dspread.demoui.utils.CustomPinKeyboardView;

import com.dspread.demoui.utils.FileUtil;
import com.dspread.demoui.utils.SPInstance;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.utils.CommonUtils;

import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;

import com.dspread.xpos.QPOSService.Display;
import com.dspread.xpos.QPOSService.DoTradeResult;
import com.dspread.xpos.QPOSService.EmvOption;
import com.dspread.xpos.QPOSService.Error;
import com.dspread.xpos.QPOSService.TransactionResult;
import com.dspread.xpos.QPOSService.TransactionType;
import com.dspread.xpos.QPOSService.UpdateInformationResult;
import com.google.android.material.navigation.NavigationView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;



public class MainActivity extends BaseActivity  {
    private QPOSService pos;
    private TextView statusEditText;
    private ListView appListView;
    private AlertDialog dialog ;

    private String nfcLog = "";
    private String amount = "";
    private String cashbackAmount = "";
    private boolean isPinCanceled = false;
    private BluetoothDevice mDevice;
    private DrawerLayout drawerLayout;
    private Button btn1,btn2,btn3,btn4,btn5,btn6,btn7,btn8,btn9,btn0,btnC,btnConfirm;
    private TextView txt_num;
    private NavigationView navigation_view;
    private String pubKey = "";
    private Intent syncDeviceTokenIntent;
    private boolean isDoTrade;
    private TextView txt_user_name;
    private Transactions transactions;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //When the window is visible to the user, keep the device normally open and keep the brightness unchanged
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON, WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        //设置不可以截屏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
        EventBus.getDefault().register(this);
        initView();
        initListener();
        DrawerArrowDrawable drawable = new DrawerArrowDrawable(MainActivity.this);
        drawable.setColor(ContextCompat.getColor(this,R.color.ds_col_7));
        getSupportActionBar().setHomeAsUpIndicator(drawable);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(getIntent() != null){
            if(ConstantsBean.getQpos() != null) {
                pos =ConstantsBean.getQpos();
                openService();
                mDevice = getIntent().getParcelableExtra("device");
                if (mDevice != null) {
                    setTitle(mDevice.getName());
                }
            }else{
                setTitle(getString(R.string.no_device_title));
                showDeviceNotActiveMsg(true);
            }
        }
        if(CheckDeviceRuntimeStatusUtil.isEmulator(this)){//检测是否是模拟器
            return;
        }
        if(pos != null) {
            MyQposClass listener = new MyQposClass();
            pos.initListener(new Handler(Looper.myLooper()), listener);
        }
        transactions = new Transactions();
        getAESTable();
    }

    private void openService(){
        TRACE.i("open service");
        if(!CommonUtils.isServiceRunning(this, SyncDeviceTokenService.class)){
            if(syncDeviceTokenIntent == null){
                syncDeviceTokenIntent = new Intent(this, SyncDeviceTokenService.class);
            }
            startService(syncDeviceTokenIntent);
            isDoTrade = false;
        }
    }

    private void getDevicesTokenStatus(String pubModel){
        TRACE.d("pub m = "+pubModel);
        GetDevicesVerifyTokenAPI.newInstance(this, pubModel, new RetrofitCallback<String>() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(String result) {
                if(result != null && pos != null){
                    TRACE.d("get envelop succcess");
                    pos.updateWorkKey(result);
                }
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {
                showDeviceNotActiveMsg(true);
                hideProgressDialog();
            }
        }).request();
    }

    private void showDeviceNotActiveMsg(boolean flag){
        if(flag){
            statusEditText.setText(getString(R.string.device_no_active));
            statusEditText.setTextColor(getResources().getColor(R.color.ds_col_38));
            Drawable d =  statusEditText.getCompoundDrawables()[0];
            DrawableCompat.setTint(d,getResources().getColor(R.color.ds_col_38));
            if(pos != null){
                pos.cancelTrade();
            }
        }else{
            statusEditText.setTextColor(getResources().getColor(R.color.ds_col_34));
            statusEditText.setText(getString(R.string.device_is_active));
            Drawable d =  statusEditText.getCompoundDrawables()[0];
            DrawableCompat.setTint(d,getResources().getColor(R.color.membership_green));
        }
    }

    @Override
    public void onToolbarLinstener() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_new_main_layout;
    }

    private void initView() {
        drawerLayout = findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.app_name,
                R.string.app_name
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        btn1 = findViewById(R.id.btn_1);
        btn0 = findViewById(R.id.btn_0);
        btn2 = findViewById(R.id.btn_2);
        btn3 = findViewById(R.id.btn_3);
        btn4 = findViewById(R.id.btn_4);
        btn5 = findViewById(R.id.btn_5);
        btn6 = findViewById(R.id.btn_6);
        btn7 = findViewById(R.id.btn_7);
        btn8 = findViewById(R.id.btn_8);
        btn9 = findViewById(R.id.btn_9);
        navigation_view = findViewById(R.id.navigation_view);
        btnConfirm = findViewById(R.id.btn_confirm);
        txt_user_name = navigation_view.getHeaderView(0).findViewById(R.id.txt_user_email);
        btnC = findViewById(R.id.btn_c);
        txt_num = findViewById(R.id.txt_num);
        statusEditText = findViewById(R.id.statusEditText);
        if(SPInstance.getInstance(this).getUserEmail() != null) {
            txt_user_name.setText(SPInstance.getInstance(this).getUserEmail());
        }
    }

    private void initListener() {
        MyOnClickListener myOnClickListener = new MyOnClickListener();
        btn0.setOnClickListener(myOnClickListener);
        btn1.setOnClickListener(myOnClickListener);
        btn2.setOnClickListener(myOnClickListener);
        btn3.setOnClickListener(myOnClickListener);
        btn4.setOnClickListener(myOnClickListener);
        btn5.setOnClickListener(myOnClickListener);
        btn6.setOnClickListener(myOnClickListener);
        btn7.setOnClickListener(myOnClickListener);
        btn8.setOnClickListener(myOnClickListener);
        btn9.setOnClickListener(myOnClickListener);
        btnC.setOnClickListener(myOnClickListener);
        btnConfirm.setOnClickListener(myOnClickListener);
        navigation_view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                if(menuItem.getItemId() == R.id.menu_connect){
                    if(pos != null){
                        close();
                    }
                    startActivity(new Intent(MainActivity.this,ScanBluetoothActivity.class));
                }else  if(menuItem.getItemId() == R.id.menu_logout){
                    if(pos != null){
                        close();
                    }
                    logout();
                }else if(menuItem.getItemId() == R.id.menu_active_device){
                    if(pos != null){
                        showProgressDialog("");
                        pos.getDevicePublicKey(20);
                    }
                }
                drawerLayout.closeDrawer(Gravity.LEFT);
                return false;
            }
        });
    }

    private void logout(){
        LogoutAPI.newInstance(this, new RetrofitCallback() {
            @Override
            public void onStart() {
                showProgressDialog("");
            }

            @Override
            public void onSuccess(Object result) {
                hideProgressDialog();
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
                SPInstance.getInstance(MainActivity.this).saveUserEmail("");
                SPInstance.getInstance(MainActivity.this).saveToken("");
                finish();
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {
                hideProgressDialog();
            }
        }).request();
    }

    /**
     * close device
     */
    private void close() {
        TRACE.d("close blue");
        if (pos == null) {
            return;
        } else  {
            pos.disconnectBT();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        MenuInflater inflater = getMenuInflater();
//        inflater.inflate(R.menu.activity_main, menu);
//        audioitem = menu.findItem(R.id.audio_test);
//        if (pos != null) {
//            if (pos.getAudioControl()) {
//                audioitem.setTitle("Audio Control : Open");
//            } else {
//                audioitem.setTitle("Audio Control : Close");
//            }
//        } else {
//            audioitem.setTitle("Audio Control : Check");
//        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        if (pos == null) {
//            Toast.makeText(getApplicationContext(), "Device Disconnect", Toast.LENGTH_LONG).show();
//            return true;
//        } else if (item.getItemId() == R.id.reset_qpos) {
//            boolean a = pos.resetPosStatus();
//            if (a) {
////                statusEditText.setText("pos reset");
//            }
//        } else if (item.getItemId() == R.id.menu_get_deivce_info) {
////            statusEditText.setText(R.string.getting_info);
//            pos.getQposInfo();
//
//        } else if (item.getItemId() == R.id.menu_get_pos_id) {
//            pos.getQposId();
////            statusEditText.setText(R.string.getting_pos_id);
//        }  else if (item.getItemId() == R.id.isCardExist) {
//            pos.isCardExist(30);
//        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onResume() {
        super.onResume();
        //
        if(!CheckCvmAppStateUtil.isTopActivity(MainActivity.this)){
            if(pos != null){
                pos.resetPosStatus();
            }
            return;
        }
        if(!CheckCvmAppStateUtil.checkTheScreenIsFull(MainActivity.this)){
            if(pos != null){
                pos.resetPosStatus();
            }
            return;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        //终止交易，断开连接
        TRACE.d("onPause");
        if(pos != null) {
            pos.cancelTrade();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        TRACE.d("onDestroy");
        EventBus.getDefault().unregister(this);
        if (pos != null) {
            close();
            pos = null;
        }
    }

    public void dismissDialog() {
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    //  deepcode ignore HardcodedValue: <comment the reason here>
    public void showDialog(String msg){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton(R.string.all_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog = builder.create();
        //  deepcode ignore HardcodedValue: <comment the reason here>
        dialog.setMessage(msg);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void uploadTransaction(Transactions transactions){
        UploadTransactionAPI.newInstance(this, transactions, new RetrofitCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Object result) {

            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {

            }
        }).request();
    }

    private String content;
    /**
     * @author qianmengChen
     * @ClassName: MyPosListener
     * @date: 2016-11-10 下午6:35:06
     */
    class MyQposClass extends CQPOSService {
        @Override
        public void onRequestUpdateWorkKeyResult(UpdateInformationResult result) {
            super.onRequestUpdateWorkKeyResult(result);
            hideProgressDialog();
            String str = "";
            if (result == UpdateInformationResult.UPDATE_SUCCESS) {
                str = ("update work key success");
            } else if (result == UpdateInformationResult.UPDATE_FAIL) {
                str = ("update work key fail");
            } else if (result == UpdateInformationResult.UPDATE_PACKET_VEFIRY_ERROR) {
                str = ("update work key packet vefiry error");
            } else if (result == UpdateInformationResult.UPDATE_PACKET_LEN_ERROR) {
                str = ("update work key packet len error");
            }
            if(str.equals("update work key success")){
                showDeviceNotActiveMsg(false);
            }else{
                showDeviceNotActiveMsg(true);
            }
            //表示先执行激活设备，激活完之后发起交易
            if(isDoActiveToken && isDoTrade){
                pos.doTrade(20);
            }
            isDoTrade = false;
            isDoActiveToken = false;
        }

        @Override
        public void onGetDevicePubKey(String clearKeys) {
            super.onGetDevicePubKey(clearKeys);
            String lenStr = clearKeys.substring(0, 4);
            int sum = 0;
            for (int i = 0; i < 4; i++) {
                int bit = Integer.parseInt(lenStr.substring(i, i + 1));
                sum += bit * Math.pow(16, (3 - i));
            }
            pubKey = clearKeys.substring(4, 4 + sum * 2);
            getDevicesTokenStatus(pubKey);
        }

        @Override
        public void onRequestWaitingUser() {//wait user to insert/swipe/tap card
            TRACE.d("onRequestWaitingUser()");
            dismissDialog();
            showProgressDialog(getString(R.string.waiting_for_card));
//            statusEditText.setText(getString(R.string.waiting_for_card));
        }

        @Override
        public void onDoTradeResult(DoTradeResult result, Hashtable<String, String> decodeData) {
            TRACE.d("(DoTradeResult result, Hashtable<String, String> decodeData) " + result.toString() + TRACE.NEW_LINE + "decodeData:" + decodeData);
            hideProgressDialog();
            String cardNo = "";
            if (result == DoTradeResult.NONE) {
                showDialog(getString(R.string.no_card_detected));
            } else if (result == DoTradeResult.ICC) {
                showProgressDialog(getString(R.string.icc_card_inserted));
                TRACE.d("EMV ICC Start");
                pos.doEmvApp(EmvOption.START);
            } else if (result == DoTradeResult.NOT_ICC) {
                showDialog(getString(R.string.card_inserted));
            } else if (result == DoTradeResult.BAD_SWIPE) {
                showDialog(getString(R.string.bad_swipe));
            } else if (result == DoTradeResult.MCR) {//磁条卡
                content = getString(R.string.card_swiped);
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40") || formatID.equals("37") || formatID.equals("17") || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");
                    content += getString(R.string.format_id) + " " + formatID + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
                    content += getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += getString(R.string.pinBlock) + " " + pinblock + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    cardNo = maskedPAN;
                } else if (formatID.equals("FF")) {
                    String type = decodeData.get("type");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    content += "cardType:" + " " + type + "\n";
                    content += "track_1:" + " " + encTrack1 + "\n";
                    content += "track_2:" + " " + encTrack2 + "\n";
                    content += "track_3:" + " " + encTrack3 + "\n";
                } else {
                    String orderID = decodeData.get("orderId");
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
//					String ksn = decodeData.get("ksn");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData.get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");
                    if (orderID != null && !"".equals(orderID)) {
                        content += "orderID:" + orderID;
                    }
                    content += getString(R.string.format_id) + " " + formatID + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN + "\n";
                    content += getString(R.string.expiry_date) + " " + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " " + cardHolderName + "\n";
//					content += getString(R.string.ksn) + " " + ksn + "\n";
                    content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += getString(R.string.trackksn) + " " + trackksn + "\n";
                    content += getString(R.string.service_code) + " " + serviceCode + "\n";
                    content += getString(R.string.track_1_length) + " " + track1Length + "\n";
                    content += getString(R.string.track_2_length) + " " + track2Length + "\n";
                    content += getString(R.string.track_3_length) + " " + track3Length + "\n";
                    content += getString(R.string.encrypted_tracks) + " " + encTracks + "\n";
                    content += getString(R.string.encrypted_track_1) + " " + encTrack1 + "\n";
                    content += getString(R.string.encrypted_track_2) + " " + encTrack2 + "\n";
                    content += getString(R.string.encrypted_track_3) + " " + encTrack3 + "\n";
                    content += getString(R.string.partial_track) + " " + partialTrack + "\n";
                    content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber + "\n";
                    cardNo = maskedPAN;
                    String realPan = null;
                }
                transactions.setHolderData(content);
                openService();
//                statusEditText.setText(content);
                showDialog("Finish Swipe Card Trade");
                uploadTransaction(transactions);
            } else if ((result == DoTradeResult.NFC_ONLINE) || (result == DoTradeResult.NFC_OFFLINE)) {
                nfcLog = decodeData.get("nfcLog");
                content = getString(R.string.tap_card);
                String formatID = decodeData.get("formatID");
                if (formatID.equals("31") || formatID.equals("40")
                        || formatID.equals("37") || formatID.equals("17")
                        || formatID.equals("11") || formatID.equals("10")) {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String trackblock = decodeData.get("trackblock");
                    String psamId = decodeData.get("psamId");
                    String posId = decodeData.get("posId");
                    String pinblock = decodeData.get("pinblock");
                    String macblock = decodeData.get("macblock");
                    String activateCode = decodeData.get("activateCode");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";

                    content += getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += "trackblock: " + trackblock + "\n";
                    content += "psamId: " + psamId + "\n";
                    content += "posId: " + posId + "\n";
                    content += getString(R.string.pinBlock) + " " + pinblock
                            + "\n";
                    content += "macblock: " + macblock + "\n";
                    content += "activateCode: " + activateCode + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    cardNo = maskedPAN;
                } else {
                    String maskedPAN = decodeData.get("maskedPAN");
                    String expiryDate = decodeData.get("expiryDate");
                    String cardHolderName = decodeData.get("cardholderName");
                    String serviceCode = decodeData.get("serviceCode");
                    String track1Length = decodeData.get("track1Length");
                    String track2Length = decodeData.get("track2Length");
                    String track3Length = decodeData.get("track3Length");
                    String encTracks = decodeData.get("encTracks");
                    String encTrack1 = decodeData.get("encTrack1");
                    String encTrack2 = decodeData.get("encTrack2");
                    String encTrack3 = decodeData.get("encTrack3");
                    String partialTrack = decodeData.get("partialTrack");
                    String pinKsn = decodeData.get("pinKsn");
                    String trackksn = decodeData.get("trackksn");
                    String pinBlock = decodeData.get("pinBlock");
                    String encPAN = decodeData.get("encPAN");
                    String trackRandomNumber = decodeData
                            .get("trackRandomNumber");
                    String pinRandomNumber = decodeData.get("pinRandomNumber");

                    content += getString(R.string.format_id) + " " + formatID
                            + "\n";
                    content += getString(R.string.masked_pan) + " " + maskedPAN
                            + "\n";
                    content += getString(R.string.expiry_date) + " "
                            + expiryDate + "\n";
                    content += getString(R.string.cardholder_name) + " "
                            + cardHolderName + "\n";
                    content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
                    content += getString(R.string.trackksn) + " " + trackksn
                            + "\n";
                    content += getString(R.string.service_code) + " "
                            + serviceCode + "\n";
                    content += getString(R.string.track_1_length) + " "
                            + track1Length + "\n";
                    content += getString(R.string.track_2_length) + " "
                            + track2Length + "\n";
                    content += getString(R.string.track_3_length) + " "
                            + track3Length + "\n";
                    content += getString(R.string.encrypted_tracks) + " "
                            + encTracks + "\n";
                    content += getString(R.string.encrypted_track_1) + " "
                            + encTrack1 + "\n";
                    content += getString(R.string.encrypted_track_2) + " "
                            + encTrack2 + "\n";
                    content += getString(R.string.encrypted_track_3) + " "
                            + encTrack3 + "\n";
                    content += getString(R.string.partial_track) + " "
                            + partialTrack + "\n";
                    content += getString(R.string.pinBlock) + " " + pinBlock
                            + "\n";
                    content += "encPAN: " + encPAN + "\n";
                    content += "trackRandomNumber: " + trackRandomNumber + "\n";
                    content += "pinRandomNumber:" + " " + pinRandomNumber
                            + "\n";
                    cardNo = maskedPAN;
                }
                transactions.setHolderData(content);
                sendMsg(8003);
            } else if ((result == DoTradeResult.NFC_DECLINED)) {
                showDialog(getString(R.string.transaction_declined));
            } else if (result == DoTradeResult.NO_RESPONSE) {
                showDialog(getString(R.string.card_no_response));
            }

        }

        /**
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestTransactionResult(com.dspread.xpos.QPOSService.TransactionResult)
         */
        @Override
        public void onRequestTransactionResult(TransactionResult transactionResult) {
            TRACE.d("onRequestTransactionResult()" + transactionResult.toString());
            if (transactionResult == TransactionResult.CARD_REMOVED) {
                clearDisplay();
            }
            hideProgressDialog();
            dismissDialog();
            if (transactionResult == TransactionResult.APPROVED) {
                TRACE.d("TransactionResult.APPROVED");
                String message = getString(R.string.transaction_approved) + "\n" + getString(R.string.amount) + ": $" + amount + "\n";
                if (!cashbackAmount.equals("")) {
                    message += getString(R.string.cashback_amount) + ": INR" + cashbackAmount;
                }
                showDialog(message);
            } else if (transactionResult == TransactionResult.TERMINATED) {
                clearDisplay();
                showDialog(getString(R.string.transaction_terminated));
            } else if (transactionResult == TransactionResult.DECLINED) {
                showDialog(getString(R.string.transaction_declined));
//                    deviceShowDisplay("DECLINED");
            } else if (transactionResult == TransactionResult.CANCEL) {
                showDialog(getString(R.string.transaction_cancel));
            } else if (transactionResult == TransactionResult.CAPK_FAIL) {
                showDialog(getString(R.string.transaction_capk_fail));
            } else if (transactionResult == TransactionResult.NOT_ICC) {
                showDialog(getString(R.string.transaction_not_icc));
            } else if (transactionResult == TransactionResult.SELECT_APP_FAIL) {
                showDialog(getString(R.string.transaction_app_fail));
            } else if (transactionResult == TransactionResult.DEVICE_ERROR) {
                showDialog(getString(R.string.transaction_device_error));
            } else if (transactionResult == TransactionResult.TRADE_LOG_FULL) {
//                statusEditText.setText("pls clear the trace log and then to begin do trade");
                showDialog("the trade log has fulled!pls clear the trade log!");
            } else if (transactionResult == TransactionResult.CARD_NOT_SUPPORTED) {
                showDialog(getString(R.string.card_not_supported));
            } else if (transactionResult == TransactionResult.MISSING_MANDATORY_DATA) {
                showDialog(getString(R.string.missing_mandatory_data));
            } else if (transactionResult == TransactionResult.CARD_BLOCKED_OR_NO_EMV_APPS) {
                showDialog(getString(R.string.card_blocked_or_no_evm_apps));
            } else if (transactionResult == TransactionResult.INVALID_ICC_DATA) {
                showDialog(getString(R.string.invalid_icc_data));
            } else if (transactionResult == TransactionResult.FALLBACK) {
                showDialog("trans fallback");
            } else if (transactionResult == TransactionResult.NFC_TERMINATED) {
                clearDisplay();
                showDialog("NFC Terminated");
            } else if (transactionResult == TransactionResult.CARD_REMOVED) {
                clearDisplay();
                showDialog("CARD REMOVED");
            }else if (transactionResult == TransactionResult.CONTACTLESS_TRANSACTION_NOT_ALLOW) {
                clearDisplay();
                showDialog("TRANS NOT ALLOW");
            }else if (transactionResult == TransactionResult.CARD_BLOCKED) {
                clearDisplay();
                showDialog("CARD BLOCKED");
            }
            amount = "";
            cashbackAmount = "";
        }

        @Override
        public void onRequestBatchData(String tlv) {
            TRACE.d("ICC trade finished");
            hideProgressDialog();
            String content = getString(R.string.batch_data);
            TRACE.d("onRequestBatchData(String tlv):" + tlv);
            content += tlv;
            Toast.makeText(MainActivity.this,"ICC trade finished",Toast.LENGTH_LONG).show();
            openService();
            showDialog("Finish ICC Card Trade");
            ClearDataUtils.clearAllCache(MainActivity.this);
            uploadTransaction(transactions);
        }

        @Override
        public void onRequestSelectEmvApp(ArrayList<String> appList) {
            TRACE.d("onRequestSelectEmvApp():" + appList.toString());
            TRACE.d("Please select App -- S，emv card config");
            Dialog dialog = new Dialog(MainActivity.this);
            dialog.setContentView(R.layout.emv_app_dialog);
            dialog.setTitle(R.string.please_select_app);
            String[] appNameList = new String[appList.size()];
            for (int i = 0; i < appNameList.length; ++i) {
                appNameList[i] = appList.get(i);
            }
            appListView = (ListView) dialog.findViewById(R.id.appList);
            appListView.setAdapter(new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_list_item_1, appNameList));
            appListView.setOnItemClickListener(new OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    pos.selectEmvApp(position);
                    TRACE.d("select emv app position = " + position);
                    dismissDialog();
                }

            });
            dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    pos.cancelSelectEmvApp();
                    dismissDialog();
                }
            });
            dialog.show();

        }

        @Override
        public void onRequestSetAmount() {
            TRACE.d("input amount -- ");
            TRACE.d("onRequestSetAmount()");
//            dismissDialog();
            transactions.setAmount(amountValue);
            pos.setAmount(amountValue, cashbackAmount, "156", TransactionType.GOODS);
//            amountValue = "";
        }

        /**
         * @see com.dspread.xpos.QPOSService.QPOSServiceListener#onRequestIsServerConnected()
         */
        @Override
        public void onRequestIsServerConnected() {
            TRACE.d("onRequestIsServerConnected()");
            pos.isServerConnected(true);
        }

        @Override
        public void onRequestOnlineProcess(final String tlv) {
            TRACE.d("onRequestOnlineProcess" + tlv);
            dismissDialog();
            String str = "8A023030";//Currently the default value,
            // should be assigned to the server to return data,
            // the data format is TLV
            pos.sendOnlineProcessResult(str);//脚本通知/55域/ICCDATA
            transactions.setHolderData(tlv);
        }

        @Override
        public void onRequestTime() {
            TRACE.d("onRequestTime");
            dismissDialog();
            String terminalTime = new SimpleDateFormat("yyyyMMddHHmmss").format(Calendar.getInstance().getTime());
            pos.sendTime(terminalTime);
            transactions.setTranTime(new Date());
//            statusEditText.setText(getString(R.string.request_terminal_time) + " " + terminalTime);
        }

        @Override
        public void onRequestDisplay(Display displayMsg) {
            TRACE.d("onRequestDisplay(Display displayMsg):" + displayMsg.toString());
            dismissDialog();
            hideProgressDialog();
            String msg = "";
            if (displayMsg == Display.CLEAR_DISPLAY_MSG) {
                msg = "";
            } else if (displayMsg == Display.MSR_DATA_READY) {
                msg = "Success,Contine ready";
            } else if (displayMsg == Display.PLEASE_WAIT) {
                msg = getString(R.string.wait);
            } else if (displayMsg == Display.REMOVE_CARD) {
                msg = getString(R.string.remove_card);
            } else if (displayMsg == Display.TRY_ANOTHER_INTERFACE) {
                msg = getString(R.string.try_another_interface);
            } else if (displayMsg == Display.PROCESSING) {
                msg = getString(R.string.processing);
            } else if (displayMsg == Display.INPUT_PIN_ING) {
                msg = "please input pin on pos";
            } else if (displayMsg == Display.INPUT_OFFLINE_PIN_ONLY || displayMsg == Display.INPUT_LAST_OFFLINE_PIN) {
                msg = "please input offline pin on pos";
            } else if (displayMsg == Display.MAG_TO_ICC_TRADE) {
                msg = "please insert chip card on pos";
            } else if (displayMsg == Display.CARD_REMOVED) {
                msg = "card removed";
            }
            showProgressDialog(msg);
        }

        @Override
        public void onRequestFinalConfirm() {
            TRACE.d("onRequestFinalConfirm() ");
            TRACE.d("onRequestFinalConfirm - S");
            dismissDialog();
            if (!isPinCanceled) {
                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.confirm_dialog);
                dialog.setTitle(getString(R.string.confirm_amount));

                String message = getString(R.string.amount) + ": $" + amount;
                if (!cashbackAmount.equals("")) {
                    message += "\n" + getString(R.string.cashback_amount) + ": $" + cashbackAmount;
                }
                ((TextView) dialog.findViewById(R.id.messageTextView)).setText(message);
                dialog.findViewById(R.id.confirmButton).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(true);
                        dialog.dismiss();
                    }
                });
                dialog.findViewById(R.id.cancelButton).setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pos.finalConfirm(false);
                        dialog.dismiss();
                    }
                });
                dialog.show();
            } else {
                pos.finalConfirm(false);
            }
        }

        @Override
        public void onRequestNoQposDetected() {
            TRACE.d("onRequestNoQposDetected()");
//            dismissDialog();
            setTitle(getString(R.string.no_device_detected));
        }

        @Override
        public void onRequestQposConnected() {
            TRACE.w("onRequestQposConnected()");
            Toast.makeText(MainActivity.this, "onRequestQposConnected", Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestQposDisconnected() {
            dismissDialog();
            TRACE.d("onRequestQposDisconnected()");
//            statusEditText.setText(getString(R.string.device_unplugged));
            setTitle(getString(R.string.no_device_title));
        }

        @Override
        public void onError(Error errorState) {
            TRACE.d("onError" + errorState.toString());
            dismissDialog();
            hideProgressDialog();
            String errorMsg = "";
            if (errorState == Error.CMD_NOT_AVAILABLE) {
                errorMsg = (getString(R.string.command_not_available));
            } else if (errorState == Error.TIMEOUT) {
                errorMsg = (getString(R.string.device_no_response));
            } else if (errorState == Error.DEVICE_RESET) {
                errorMsg = (getString(R.string.device_reset));
            } else if (errorState == Error.UNKNOWN) {
                errorMsg = (getString(R.string.unknown_error));
            } else if (errorState == Error.DEVICE_BUSY) {
                errorMsg = (getString(R.string.device_busy));
            } else if (errorState == Error.INPUT_OUT_OF_RANGE) {
                errorMsg = (getString(R.string.out_of_range));
            } else if (errorState == Error.INPUT_INVALID_FORMAT) {
                errorMsg = (getString(R.string.invalid_format));
            } else if (errorState == Error.INPUT_ZERO_VALUES) {
                errorMsg = (getString(R.string.zero_values));
            } else if (errorState == Error.INPUT_INVALID) {
                errorMsg = (getString(R.string.input_invalid));
            } else if (errorState == Error.CASHBACK_NOT_SUPPORTED) {
                errorMsg = (getString(R.string.cashback_not_supported));
            } else if (errorState == Error.CRC_ERROR) {
                errorMsg = (getString(R.string.crc_error));
            } else if (errorState == Error.COMM_ERROR) {
                errorMsg = (getString(R.string.comm_error));
            } else if (errorState == Error.MAC_ERROR) {
                errorMsg = (getString(R.string.mac_error));
            } else if (errorState == Error.APP_SELECT_TIMEOUT) {
                errorMsg = (getString(R.string.app_select_timeout_error));
            } else if (errorState == Error.CMD_TIMEOUT) {
                errorMsg = (getString(R.string.cmd_timeout));
            } else if (errorState == Error.ICC_ONLINE_TIMEOUT) {
                if (pos == null) {
                    return;
                }
                pos.resetPosStatus();
                errorMsg = (getString(R.string.device_reset));
            }
            isDoTrade = false;
            Toast.makeText(MainActivity.this,errorMsg,Toast.LENGTH_LONG).show();
        }

        @Override
        public void onReturnReversalData(String tlv) {
            String content = getString(R.string.reversal_data);
            content += tlv;
            TRACE.d("onReturnReversalData(): " + tlv);
//            statusEditText.setText(content);
        }

        @Override
        public void onReturnGetPinResult(Hashtable<String, String> result) {
            TRACE.d("onReturnGetPinResult(Hashtable<String, String> result):" + result.toString());
            String pinBlock = result.get("pinBlock");
            String pinKsn = result.get("pinKsn");
            String content = "get pin result\n";
            content += getString(R.string.pinKsn) + " " + pinKsn + "\n";
            content += getString(R.string.pinBlock) + " " + pinBlock + "\n";
//            statusEditText.setText(content);
            TRACE.i(content);
        }

        @Override
        public void onGetCardNoResult(String cardNo) {
            TRACE.d("onGetCardNoResult(String cardNo):" + cardNo);
//            statusEditText.setText("cardNo: " + cardNo);
        }

        @Override
        public void onRequestSetPin() {
            TRACE.i("onRequestSetPin()");
            final CustomPinKeyboardDialog dialog=new CustomPinKeyboardDialog(MainActivity.this);
            dialog.getPayViewPass()
                    .setRandomNumber(true)
                    .setPayClickListener(new CustomPinKeyboardView.OnPayClickListener() {
                        @Override
                        public void onPassFinish(final String password) {
                            dialog.dismiss();
                            Toast.makeText(MainActivity.this,"input : "+password,Toast.LENGTH_LONG).show();
                            List<String> datas = pos.getPinTransmissData();
                            TRACE.i("data = "+datas.get(0));
                            String result = decryptWithWhiteAesKey(datas.get(0));
                            TRACE.w("decrypt result = "+result);
                            int index = 0;
                            int randomDataLen = Integer.valueOf(result.substring(index,2),16) *2;
                            String randomData = result.substring(2,2+randomDataLen);
                            index=randomDataLen+2;
                            int aesKeyLen = Integer.valueOf(result.substring(index,index + 2),16) *2;
                            index += 2;
                            String aesKey = result.substring(index,index + aesKeyLen);
                            index += aesKeyLen;
                            int panTokenLen = Integer.valueOf(result.substring(index,index + 2),16) *2;
                            index +=2;
                            String panToken = result.substring(index,index + panTokenLen);
                            index += panTokenLen;
                            // 随机表生成加密pin
                            int keyListLen = Integer.valueOf(result.substring(index, index + 2), 16) * 2;
                            index += 2;
                            String keyList = result.substring(index, index + keyListLen);
                            keyList = CommonUtils.convertHexToString(keyList);
                            String newPin = "";
                            for(int i = 0 ; i < password.length() ; i ++){
                                for(int j = 0 ; j < keyList.length() ; j ++){
                                    if(keyList.charAt(j) == password.charAt(i)){
                                        newPin = newPin + Integer.toHexString(j)+"";
                                        break;
                                    }
                                }
                            }
                            //白盒算法去解密上面数据，得到pan token，aes key和随机数 ，下面使用iso format-4算法去加密pin得到pinblock
                            String encryptPin = CommonUtils.buildCvmPinBlock(randomData,panToken,aesKey,newPin);
                            transactions.setPin(encryptPin);
                            pos.sendEncryptPinByAES(encryptPin);
                        }
                        @Override
                        public void onPayClose() {
                            dialog.dismiss();
                        }
                    });
        }

        @Override
        public void onReturniccCashBack(Hashtable<String, String> result) {
            TRACE.d("onReturniccCashBack(Hashtable<String, String> result):" + result.toString());
            String s = "serviceCode: " + result.get("serviceCode");
            s += "\n";
            s += "trackblock: " + result.get("trackblock");
        }

        @Override
        public void onEmvICCExceptionData(String arg0) {
            TRACE.d("onEmvICCExceptionData(String arg0):" + arg0);
        }

        @Override
        public void onQposIsCardExist(boolean cardIsExist) {
            TRACE.d("onQposIsCardExist(boolean cardIsExist):" + cardIsExist);
            if (cardIsExist) {
//                statusEditText.setText("cardIsExist:" + cardIsExist);
            } else {
//                statusEditText.setText("cardIsExist:" + cardIsExist);
            }
        }

        @Override
        public void onQposKsnResult(Hashtable<String, String> arg0) {
            TRACE.d("onQposKsnResult(Hashtable<String, String> arg0):" + arg0.toString());
            String pinKsn = arg0.get("pinKsn");
            String trackKsn = arg0.get("trackKsn");
            String emvKsn = arg0.get("emvKsn");
            TRACE.d("get the ksn result is :" + "pinKsn" + pinKsn + "\ntrackKsn" + trackKsn + "\nemvKsn" + emvKsn);
        }

        @Override
        public void onTradeCancelled() {
            TRACE.d("onTradeCancelled");
            dismissDialog();
        }

    }

    private void clearDisplay() {
        statusEditText.setText("");
    }
    class MyOnClickListener implements OnClickListener {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View v) {
            getAmountText((Button) v);
        }
    }

    //设置键盘金额输入
    private String amountValue = "";
    private void getAmountText(Button btn){
        if(btn.getId() == R.id.btn_c){
            amountValue = "";
            txt_num.setText(amountValue);
        }else if(btn.getId() == R.id.btn_confirm){
            isDoTrade = true;
//            showDeviceNotActiveMsg(false);
            //表示设备先发交易，所以先关闭service，完成交易再激活
            if(!isDoActiveToken) {
                if(syncDeviceTokenIntent != null) {
                    stopService(syncDeviceTokenIntent);
                }
                if(pos != null) {
                    pos.doTrade(20);
                }
            }
        }else{
            if(amountValue.length() < 14){
                amountValue += btn.getText().toString();
            }
            Long a = Long.parseLong(amountValue);
            String amountValueStr = a/100 + "."+a%100;
            txt_num.setText(amountValueStr);
        }
    }

    private void sendMsg(int what) {
        Message msg = new Message();
        msg.what = what;
        mHandler.sendMessage(msg);
    }

    /**
     * Get the aes table from backend
     */
    private void getAESTable(){
        GetAESTableAPI.newInstance(this, new RetrofitCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Object result) {
                FileUtil.writeFile("aes-table-table", (byte[]) result,MainActivity.this);
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {

            }
        }).request();
    }

    private AES aes;

    /**
     * use the white ase key to decrypt the data
     * @param str
     * @return
     */
    private String decryptWithWhiteAesKey(String str) {
        if (aes == null) {
            aes = WhiteBoxAESUtil.readAESTable(getApplicationContext());
            if(aes == null){
                return null;
            }
        }
        byte[] plainBytes;
        plainBytes = CommonUtils.HexStringToByteArray(str);
        TRACE.i("plain = "+CommonUtils.byteArray2Hex(plainBytes));
        return WhiteBoxAESUtil.whiteBoxDecrypt(plainBytes,aes);
    }

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 8003:
                    TRACE.w("nfc 8003");
                    try {
                        Thread.sleep(200);
                        //  deepcode ignore catchingInterruptedExceptionWithoutInterrupt: <comment the reason here>
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    String content = "";
                    if (nfcLog == null) {
                        Hashtable<String, String> h = pos.getNFCBatchData();
                        String tlv = h.get("tlv");
                        TRACE.i("nfc batchdata1: " + tlv);
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + h.get("tlv");
                    } else {
                        content = statusEditText.getText().toString() + "\nNFCbatchData: " + nfcLog;
                    }
                    showDialog("Finish NFC Card Trade");
                    openService();
                    uploadTransaction(transactions);
                    break;
                default:
                    break;
            }
        }
    };

    //下面是开启service后激活状态的eventbus的回调
    private boolean isDoActiveToken = false;
    @Subscribe
    public void onSyncTokenActiveStatus(DeviceTokenStatusEvent event){
        if(event.isTokenActive){
            if(pos != null){
                pos.getDevicePublicKey(20);
                statusEditText.setTextColor(getResources().getColor(R.color.ds_col_34));
                isDoActiveToken = true;
            }
        }else{
            isDoActiveToken = false;
        }
    }

}
