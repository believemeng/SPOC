package com.dspread.demoui.net.retrofitUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.WindowManager;

import com.dspread.demoui.activities.LoginActivity;
import com.dspread.demoui.utils.SPInstance;

/**
 * <code>Doc</code>：TODO
 *
 * @author 94585
 * @version 1.0
 * @date 2020-10-15
 */
public class RetrofitAuthUtil {

    private static RetrofitAuthUtil instance;
    private Context context;

    private RetrofitAuthUtil(Context context) {
        this.context = context;
    }

    public static synchronized RetrofitAuthUtil getInstance(Context context) {
        if (instance == null) {
            instance = new RetrofitAuthUtil(context);
        }
        return instance;
    }


    public static void showReLogoutDialog(final Context context, String title, String content) {
//        if (!((Activity) context).isFinishing()) {
//
//        }
        RetrofitAuthUtil.getInstance(context).logout();
        relogin(context);
//        AlertDialog.Builder builder = new AlertDialog.Builder(context);
//        builder.setTitle(title);
//        builder.setMessage(content);
//        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                RetrofitAuthUtil.getInstance(context).logout();
//                relogin(context);
//                dialog.dismiss();
//            }
//        });
//        Dialog dialog = builder.create();
//        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
//        dialog.show();
    }


    public static void relogin(Context context) {
//        Intent intent = new Intent(context.getApplicationContext(), SignInActivity.class);
        Intent intent = new Intent(context.getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public boolean isLoggedIn() {
        long currentTime = System.currentTimeMillis();
        long expiryTime = SPInstance.getInstance(context).getTokenExpiryDate();
        if (expiryTime < currentTime) {
            return false;
        }
        return SPInstance.getInstance(context).isLoggedIn();
    }

    public void logout() {
        SPInstance.getInstance(context).saveToken(null);
    }
}
