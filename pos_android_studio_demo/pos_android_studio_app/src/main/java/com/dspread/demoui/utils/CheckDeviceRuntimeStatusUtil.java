package com.dspread.demoui.utils;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
//import android.support.annotation.RequiresApi;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.WindowManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.List;

/**
 * Time:2020/8/28
 * Author:Qianmeng Chen
 * Description:
 */
public class CheckDeviceRuntimeStatusUtil {
    //判断当前设备是否是模拟器。如果返回TRUE，则当前是模拟器，不是返回FALSE
    public static boolean isEmulator(Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                String imei = tm.getDeviceId();
                if (imei != null && imei.equals("000000000000000")){
                    return true;
                }
                return true;
            }

            return  (Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"));
        }catch (Exception ioe) {

        }
        return false;
    }

    /**
     * Is rooted boolean.
     * 设备是否被root
     *
     * @return the boolean
     */
    public static boolean isRooted() {
        // nexus 5x "/su/bin/"
        String[] paths = {"/system/xbin/", "/system/bin/", "/system/sbin/", "/sbin/", "/vendor/bin/", "/su/bin/"};
        try {
            for (int i = 0; i < paths.length; i++) {
                String path = paths[i] + "su";
                if (new File(path).exists()) {
                    String execResult = exec(new String[]{"ls", "-l", path});
                    if (TextUtils.isEmpty(execResult) || execResult.indexOf("root") == execResult.lastIndexOf("root")) {
                        return false;
                    }
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private static String exec(String[] exec) {
        String ret = "";
        ProcessBuilder processBuilder = new ProcessBuilder(exec);
        try {
            Process process = processBuilder.start();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                ret += line;
            }
            process.getInputStream().close();
            process.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ret;
    }


}
