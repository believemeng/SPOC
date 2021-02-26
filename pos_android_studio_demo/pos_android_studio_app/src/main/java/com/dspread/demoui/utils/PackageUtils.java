package com.dspread.demoui.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Time:2020/8/21
 * Author:Qianmeng Chen
 * Description:获取包信息和版本
 */
public class PackageUtils {
    public static String getVersion(Context appContext) {
//        String version = "1.0.0";
//        PackageManager packageManager = appContext.getPackageManager();
//        try {
//            PackageInfo packageInfo = packageManager.getPackageInfo(
//                    appContext.getPackageName(), 0);
//            version = packageInfo.versionName;
//        } catch (NameNotFoundException e) {
//            e.printStackTrace();
//        }
        String version = "3.8.0";

//        String version = appContext.getResources().getString(R.string.token_version_number);


        return version;
    }

    public static int getVersionCode(Context appContext) {
        int version = 1000;
        PackageManager packageManager = appContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    appContext.getPackageName(), 0);
            version = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
           // e.printStackTrace();
        }
        return version;
    }

    public static String getPackageName(Context appContext) {
        String packageName = "com.wanda.sdk";
        PackageManager packageManager = appContext.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    appContext.getPackageName(), 0);
            packageName = packageInfo.packageName;
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        }
        return packageName;
    }
}
