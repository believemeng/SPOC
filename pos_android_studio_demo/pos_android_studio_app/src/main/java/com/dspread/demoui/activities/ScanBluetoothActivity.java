package com.dspread.demoui.activities;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
//import android.support.annotation.NonNull;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
//import androidx.core.widget.SwipeRefreshLayout;
//import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
//import android.support.v7.widget.DividerItemDecoration;
//import android.support.v7.widget.LinearLayoutManager;
//import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.adapters.BluetoothListAdapter;
import com.dspread.demoui.beans.ConstantsBean;
import com.dspread.demoui.beans.PosInfos;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.apis.VerifySCRPAPI;
import com.dspread.demoui.utils.SPInstance;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.widget.CustomLayoutManager;
import com.dspread.xpos.CQPOSService;
import com.dspread.xpos.QPOSService;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ScanBluetoothActivity extends BaseActivity {
    private RecyclerView blu_recycleview;
    private LinearLayout lin_empty;
    private static final int LOCATION_CODE = 101;
    private LocationManager lm;//【位置管理】
    private QPOSService pos;
    private SwipeRefreshLayout swipeContainer;
    private BluetoothListAdapter adapter;
    private List<BluetoothDevice> deviceList = new ArrayList<>();
    private BluetoothDevice mDevice;
    private PosInfos posInfos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        setTitle(getString(R.string.scan_blu_list));
        blu_recycleview = findViewById(R.id.blu_recycleview);
        lin_empty = findViewById(R.id.lin_empty);
        swipeContainer = findViewById(R.id.swipeContainer);
        requestBluPermission();
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
//                showProgressDialog("");
                if(pos != null) {
                    pos.clearBluetoothBuffer();
                    pos.scanQPos2Mode(ScanBluetoothActivity.this, 30);
                }
            }
        });
    }

    @Override
    public void onToolbarLinstener() {
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_scan_bluetooth;
    }

    public void requestBluPermission() {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null && !adapter.isEnabled()) {//表示蓝牙不可用 add one fix
            Intent enabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(enabler);
        }
        lm = (LocationManager) ScanBluetoothActivity.this.getSystemService(ScanBluetoothActivity.this.LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (ContextCompat.checkSelfPermission(ScanBluetoothActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                Log.e("POS_SDK", "没有权限");
                // 没有权限，申请权限。
                // 申请授权。
                ActivityCompat.requestPermissions(ScanBluetoothActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION,android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_CODE);
            } else {
                scanBlue();
            }
        } else {
            Log.e("BRG", "系统检测到未开启定位服务");
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, 1315);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限被用户同意。
                    scanBlue();
                    Toast.makeText(ScanBluetoothActivity.this, getString(R.string.msg_allowed_location_permission), Toast.LENGTH_LONG).show();
                } else {
                    // 权限被用户拒绝了。
                    Toast.makeText(ScanBluetoothActivity.this, getString(R.string.msg_not_allowed_loaction_permission), Toast.LENGTH_LONG).show();
                }
            }
            break;
        }
    }

    private void scanBlue(){
        open(QPOSService.CommunicationMode.BLUETOOTH);
    }

    /**
     * open and init bluetooth
     *
     * @param mode
     */
    private void open(QPOSService.CommunicationMode mode) {
        TRACE.d("open");
        MyQposClass listener = new MyQposClass();
        pos = QPOSService.getInstance(mode);
        if (pos == null) {
            return;
        }
        pos.setConext(ScanBluetoothActivity.this);
        //init handler
        Handler handler = new Handler(Looper.myLooper());
        pos.initListener(handler, listener);
//        String sdkVersion = pos.getSdkVersion();
//        Toast.makeText(ScanBluetoothActivity.this, "sdkVersion--" + sdkVersion, Toast.LENGTH_SHORT).show();
        pos.clearBluetoothBuffer();
        pos.scanQPos2Mode(ScanBluetoothActivity.this,20);
        showProgressDialog("");
        deviceList = pos.getDeviceList();
        adapter = new BluetoothListAdapter(this,deviceList);
        adapter.setOnItemClickListener(new BluetoothListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BluetoothDevice device) {
                pos.stopScanQPos2Mode();
                mDevice = device;
                pos.connectBluetoothDevice(true, 25, device.getAddress());
                showProgressDialog("");
            }
        });
        blu_recycleview.setLayoutManager(new CustomLayoutManager(this, LinearLayoutManager.VERTICAL,false));
        blu_recycleview.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        blu_recycleview.setAdapter(adapter);
    }

    class MyQposClass extends CQPOSService{
        @Override
        public void onRequestQposConnected() {
            super.onRequestQposConnected();
            Toast.makeText(ScanBluetoothActivity.this,getString(R.string.connected),Toast.LENGTH_LONG).show();
            pos.getQposInfo();
            ConstantsBean.setQpos(pos);
        }

        @Override
        public void onRequestQposDisconnected() {
            super.onRequestQposDisconnected();
            Toast.makeText(ScanBluetoothActivity.this,getString(R.string.connected_fail),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onRequestNoQposDetected() {
            super.onRequestNoQposDetected();
            Toast.makeText(ScanBluetoothActivity.this,getString(R.string.connected_fail),Toast.LENGTH_LONG).show();
        }

        @Override
        public void onQposInfoResult(Hashtable<String, String> posInfoData) {
            super.onQposInfoResult(posInfoData);
            TRACE.d("onQposInfoResult" + posInfoData.toString());
            String isSupportedTrack1 = posInfoData.get("isSupportedTrack1") == null ? "" : posInfoData.get("isSupportedTrack1");
            String isSupportedTrack2 = posInfoData.get("isSupportedTrack2") == null ? "" : posInfoData.get("isSupportedTrack2");
            String isSupportedTrack3 = posInfoData.get("isSupportedTrack3") == null ? "" : posInfoData.get("isSupportedTrack3");
            String bootloaderVersion = posInfoData.get("bootloaderVersion") == null ? "" : posInfoData.get("bootloaderVersion");
            String firmwareVersion = posInfoData.get("firmwareVersion") == null ? "" : posInfoData.get("firmwareVersion");
            String isUsbConnected = posInfoData.get("isUsbConnected") == null ? "" : posInfoData.get("isUsbConnected");
            String isCharging = posInfoData.get("isCharging") == null ? "" : posInfoData.get("isCharging");
            String batteryLevel = posInfoData.get("batteryLevel") == null ? "" : posInfoData.get("batteryLevel");
            String batteryPercentage = posInfoData.get("batteryPercentage") == null ? ""
                    : posInfoData.get("batteryPercentage");
            String hardwareVersion = posInfoData.get("hardwareVersion") == null ? "" : posInfoData.get("hardwareVersion");
            String SUB = posInfoData.get("SUB") == null ? "" : posInfoData.get("SUB");
            String pciFirmwareVersion = posInfoData.get("PCI_firmwareVersion") == null ? ""
                    : posInfoData.get("PCI_firmwareVersion");
            String pciHardwareVersion = posInfoData.get("PCI_hardwareVersion") == null ? ""
                    : posInfoData.get("PCI_hardwareVersion");
            posInfos = new PosInfos();
            posInfos.setBootLoaderVersion(bootloaderVersion);
            posInfos.setFirmwareVersion(firmwareVersion);
            posInfos.setHardwareVersion(hardwareVersion);
            posInfos.setPCIFirmwareVresion(pciFirmwareVersion);
            posInfos.setPCIHardwareVersion(pciHardwareVersion);
            pos.getQposId();
        }

        @Override
        public void onQposIdResult(Hashtable<String, String> posIdTable) {
            super.onQposIdResult(posIdTable);
            String posId = posIdTable.get("posId") == null ? "" : posIdTable.get("posId");
            String csn = posIdTable.get("csn") == null ? "" : posIdTable.get("csn");
            String psamId = posIdTable.get("psamId") == null ? "" : posIdTable
                    .get("psamId");
            String NFCId = posIdTable.get("nfcID") == null ? "" : posIdTable
                    .get("nfcID");
            posInfos.setTerminalId(posId);
            SPInstance.getInstance(ScanBluetoothActivity.this).saveTerminalId(posId);
            verifySCRP(posInfos);
        }

        @Override
        public void onDeviceFound(BluetoothDevice device) {
            super.onDeviceFound(device);
            TRACE.w("device = "+device.getName()+"  "+device.getAddress());
            if(device.getName() != null) {
                deviceList.add(device);
            }
            lin_empty.setVisibility(View.GONE);
            blu_recycleview.setVisibility(View.VISIBLE);
            if(adapter != null){
                List<BluetoothDevice> list =  removeDuplicate(deviceList);
                adapter.addDevice(list);
            }
        }

        @Override
        public void onRequestDeviceScanFinished() {
            super.onRequestDeviceScanFinished();
            TRACE.w("scan finish ==");
            swipeContainer.setRefreshing(false);
            hideProgressDialog();
            if(adapter != null){
                if(adapter.getItemCount() == 0){
                    lin_empty.setVisibility(View.VISIBLE);
                    blu_recycleview.setVisibility(View.GONE);
                }else {
                    lin_empty.setVisibility(View.GONE);
                    blu_recycleview.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public List<BluetoothDevice> removeDuplicate(List<BluetoothDevice> list){
        Set set = new LinkedHashSet<BluetoothDevice>();
        set.addAll(list);
        list.clear();
        list.addAll(set);
        return list;
    }

    private void verifySCRP(PosInfos posInfos){
        VerifySCRPAPI.newInstance(this, posInfos, new RetrofitCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Object result) {
                Intent intent = new Intent(ScanBluetoothActivity.this,MainActivity.class);
                intent.putExtra("device",mDevice);
                startActivity(intent);
                finish();
                hideProgressDialog();
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {
                pos.disconnectBT();
            }
        }).request();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(pos != null){
            pos.stopScanQPos2Mode();
        }
    }
}