package com.dspread.demoui.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.dspread.demoui.R;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.apis.LoginAPI;
import com.dspread.demoui.net.apis.VerifyAppAPI;
import com.dspread.demoui.services.SyncDeviceStateService;
import com.dspread.demoui.utils.CheckCvmAppStateUtil;
import com.dspread.demoui.utils.CustomPinKeyboardDialog;
import com.dspread.demoui.utils.CustomPinKeyboardView;
import com.dspread.demoui.utils.SPInstance;
import com.dspread.demoui.utils.CommonUtils;
import com.dspread.demoui.utils.TRACE;
import com.dspread.demoui.widget.ClearableEditText;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends BaseActivity implements View.OnClickListener, CommonUtils.ChooseVerifyStatusInterface {
    private EditText edt_password;
    private boolean isShowPassword;
    private ClearableEditText edt_username;
    private Button btn_signin;
    private ImageView btn_show_password;
    private TextView btn_forgot_password;
    private TextView btn_sign_up;
    private String email,pwd;
    private boolean isSignUp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        edt_password = findViewById(R.id.edt_password);
        edt_username = findViewById(R.id.edt_username);
        btn_signin = findViewById(R.id.btn_signin);
        btn_forgot_password = findViewById(R.id.btn_forgot_password);
        btn_sign_up = findViewById(R.id.btn_sign_up);

        btn_show_password = findViewById(R.id.btn_show_password);
        btn_show_password.setSelected(!isShowPassword);
        btn_show_password.setOnClickListener(this);
        btn_signin.setOnClickListener(this);
        btn_forgot_password.setOnClickListener(this);
        btn_sign_up.setOnClickListener(this);
        if(getIntent() != null){
            email = getIntent().getStringExtra("email");
            isSignUp = getIntent().getBooleanExtra("signUp",false);
            if(email != null){
                edt_username.setText(email);
            }
        }
        CommonUtils.setVerifyStatusListener(this);
        if(SPInstance.getInstance(LoginActivity.this).getUserEmail() != null && !SPInstance.getInstance(LoginActivity.this).getUserEmail().equals("")
                && SPInstance.getInstance(LoginActivity.this).getToken() != null && !SPInstance.getInstance(LoginActivity.this).getToken().equals("")){

        }else {
            List<String> item = new ArrayList<>();
            item.add("Verify");
            item.add("No Verify");
            CommonUtils.showRadioButtonDialog(this,"Choose if do the attestion",item);
        }
    }

    private void doAttestion(){
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O_MR1){
            CommonUtils.showMyErrorDialog(this,"","The apk is not safety, will close the app!");
        }

        //check if is downloaded from the google play
        if(!CheckCvmAppStateUtil.isStoreVersion(this)){
            CommonUtils.showMyErrorDialog(this,"","The apk is not from the google play, will close the app!");
        }

        //check the apk sign
        String sign = CheckCvmAppStateUtil.checkApkSign(this);
        verifyAppSign(sign);
    }

   @Override
    public void onToolbarLinstener() {

    }

    @Override
    protected int getLayoutId() {
        String resukt =  CheckCvmAppStateUtil.checkApkSign(this);
        TRACE.i("result = "+resukt);
        if(getIntent() != null){
            isSignUp = getIntent().getBooleanExtra("signUp",false);
        }
        if(!isSignUp && SPInstance.getInstance(LoginActivity.this).getUserEmail() != null && !SPInstance.getInstance(LoginActivity.this).getUserEmail().equals("")
                && SPInstance.getInstance(LoginActivity.this).getToken() != null && !SPInstance.getInstance(LoginActivity.this).getToken().equals("")){
            startActivity(new Intent(LoginActivity.this,MainActivity.class));
            if(!CommonUtils.isServiceRunning(LoginActivity.this, SyncDeviceStateService.class)) {
                Intent syncAttendeeListIntent = new Intent(LoginActivity.this, SyncDeviceStateService.class);
                startService(syncAttendeeListIntent);
            }
            finish();
        }
        return R.layout.activity_login;
    }

    private void showPassword() {
        int start = edt_password.getSelectionStart();
        int end = edt_password.getSelectionEnd();
        if (isShowPassword) {
            edt_password.setTransformationMethod(new PasswordTransformationMethod());
        } else {
            edt_password.setTransformationMethod(null);
        }
        edt_password.setSelection(start, end);
        isShowPassword = !isShowPassword;
        btn_show_password.setSelected(!isShowPassword);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_show_password:
                showPassword();
                break;
            case R.id.btn_signin:
                if(validateInput()){
                    requestLogin();
                }
                break;
            case R.id.btn_forgot_password:
                forgotPassword();
                break;
            case R.id.btn_sign_up:
                Intent intent = new Intent(LoginActivity.this,SignUpActivity.class);
                startActivity(intent);
                break;
        }
    }

    private boolean validateInput(){
        email = edt_username.getText().toString();
        pwd = edt_password.getText().toString();
        boolean isValid = true;
        if (TextUtils.isEmpty(email)) {
            String errorInfo = getString(R.string.invalid_email_msg);
            edt_username.setError(errorInfo);
            edt_username.requestFocus();
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            String errorInfo = getString(R.string.login_email_wrong);
            edt_username.setError(errorInfo);
            edt_username.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(pwd)) {
            String errorInfo = getString(R.string.login_wrong_password_msg);
            edt_password.setError(errorInfo);
            edt_password.requestFocus();
            btn_show_password.setVisibility(View.GONE);
            isValid = false;
        }
        return isValid;
    }

    private boolean validateEmail(){
        if(email == null || email.isEmpty()){
            return false;
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            return false;
        }
        return true;
    }

    public void forgotPassword() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.forgot_password));
        builder.setMessage(getResources().getString(R.string.input_your_email));
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_forgot_password, null);
        builder.setView(view);
        final EditText etEmail = view.findViewById(R.id.email_sent);
        builder.setNegativeButton(R.string.all_cancel, null);
        builder.setPositiveButton(R.string.action_send, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(validateEmail()){

                }
            }
        });
        builder.create().show();
    }

    private void requestLogin(){
        //get the password md5 value to send backend
        pwd = CommonUtils.getSignature(pwd);
        LoginAPI.newInstance(this, email, pwd, new RetrofitCallback() {
            @Override
            public void onStart() {
                showProgressDialog("");
            }

            @Override
            public void onSuccess(Object result) {
                hideProgressDialog();
                SPInstance.getInstance(LoginActivity.this).saveUserEmail(email);
                Intent intent = new Intent(LoginActivity.this,ScanBluetoothActivity.class);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {
                hideProgressDialog();
            }
        }).request();
    }

    private void verifyAppSign(String sign){
        VerifyAppAPI.newInstance(this, sign, new RetrofitCallback() {
            @Override
            public void onStart() {

            }

            @Override
            public void onSuccess(Object result) {
                boolean a = (boolean) result;
                if(!a){
                    CommonUtils.showMyErrorDialog(LoginActivity.this,"","The apk is not safety, will close the app!");
                }else {
//                    if(!CommonUtils.isServiceRunning(LoginActivity.this, SyncDeviceStateService.class) && !isSignUp) {
//                        if(SPInstance.getInstance(LoginActivity.this).getToken() != null && !SPInstance.getInstance(LoginActivity.this).getToken().equals("")) {
//                            Intent syncAttendeeListIntent = new Intent(LoginActivity.this, SyncDeviceStateService.class);
//                            startService(syncAttendeeListIntent);
//                        }
//                    }
                }
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {

            }
        }).request();
    }

    @Override
    public void onVerifyStatusListener(boolean isVerify) {
        if(isVerify){
            doAttestion();
        }
    }

    @Override
    protected void onDestroy() {
        if(CommonUtils.dialog != null){
            CommonUtils.dialog.dismiss();
        }
        super.onDestroy();
    }
}