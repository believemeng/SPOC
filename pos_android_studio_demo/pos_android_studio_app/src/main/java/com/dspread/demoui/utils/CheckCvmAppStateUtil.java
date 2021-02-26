package com.dspread.demoui.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.os.Build;
import android.text.TextUtils;
import android.view.WindowManager;

import androidx.annotation.RequiresApi;

import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Locale;

/**
 * Time:2020/7/20
 * Author:Qianmeng Chen
 * Description:
 */
public class CheckCvmAppStateUtil {
    private CheckCvmAppStateUtil cvmApp;

    //用单例模式
    public CheckCvmAppStateUtil getInstance(){
        if(cvmApp == null){
            cvmApp = new CheckCvmAppStateUtil();
        }
        return cvmApp;
    }
//    /**
//     * Is foreground boolean.
//     *
//     * @param context the context
//     * @return the boolean
//     */
//    /*判断应用是否在前台*/
//    private  boolean isForeground(Context context) {
//        try {
//            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//            assert am != null;
//            List<ActivityManager.RunningTaskInfo> tasks = am.getRunningTasks(1);
//            if (!tasks.isEmpty()) {
//                ComponentName topActivity = tasks.get(0).topActivity;
//                if (topActivity.getPackageName().equals(context.getPackageName())) {
//                    return true;
//                }
//            }
//            return false;
//        } catch (SecurityException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }


    /**
     * 判断是否是google play商店下载的app
     * @param
     */
    public static boolean isStoreVersion(Context context) {
        boolean result = false;
        try {
            String installer = context.getPackageManager()
                    .getInstallerPackageName(context.getPackageName());
            TRACE.w("store version a = "+installer +" package = "+context.getPackageName());
            if(null != installer && installer.equals("com.android.vending")){
                result = true;
            }
            TRACE.i("store version a = "+result);
        } catch (@NotNull Throwable e) {
        }
        return result;
    }



    //get the sha1 sign of the apk keystore
    public static String checkApkSign(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);
            byte[] cert = info.signatures[0].toByteArray();
            //  deepcode ignore InsecureHash: <comment the reason here>
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);

            StringBuilder hexString = new StringBuilder();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            String result = hexString.toString();
            return result.substring(0, result.length()-1);
        } catch (PackageManager.NameNotFoundException e) {
            //e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            //e.printStackTrace();
        }
        return null;
    }

    /**
     * 和key的签名文件对比
     * @param s
     */
    public static void compareApkSign(String s,Context context){
        if (!s.equals("你获取的SHA1")){
            killAppProcess(context);
        }
    }
    /**
     * 杀死进程
     */
    public static void killAppProcess(Context context)
    {
        //注意：不能先杀掉主进程，否则逻辑代码无法继续执行，需先杀掉相关进程最后杀掉主进程
        ActivityManager mActivityManager = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> mList = mActivityManager.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : mList)
        {
            if (runningAppProcessInfo.pid != android.os.Process.myPid())
            {
                android.os.Process.killProcess(runningAppProcessInfo.pid);
            }
        }
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    //判断当前程序是否在前台运行
    public static boolean isTopActivity(Context context){
        ActivityManager mActivityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> tasksInfo = mActivityManager.getRunningTasks(1);
        if(tasksInfo.size() > 0){
            //应用程序位于堆栈的顶层
            if(context.getPackageName().equals(tasksInfo.get(0).topActivity.getPackageName())){
                return true;
            }
        }
        return false;
    }

    /**
     * check the app if is run in full screen
     * @param
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static  boolean checkTheScreenIsFull(Context context){
        WindowManager mWm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        int mFullScreenSizeWidth  = mWm.getDefaultDisplay().getMode().getPhysicalWidth();
        int mFullScreenSizeHeight = mWm.getDefaultDisplay().getMode().getPhysicalHeight();
        Point mDisplaySize = new Point();
        if (mDisplaySize.x == mFullScreenSizeWidth && mDisplaySize.y == mFullScreenSizeHeight) {
            //全屏逻辑
            return true;
        }
        return false;
    }

}
