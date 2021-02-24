package com.dspread.demoui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.beans.Users;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.apis.SignUpAPI;
import com.dspread.demoui.utils.CommonUtils;
import com.dspread.demoui.widget.ClearableEditText;

public class SignUpActivity extends BaseActivity implements View.OnClickListener {
    private ClearableEditText edt_email,edit_pwd,edt_phone,edit_company;
    private Button btn_signup;
    private String email,pwd;
    private Users users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_up);
        edt_email = findViewById(R.id.edt_email);
        edit_pwd = findViewById(R.id.edit_pwd);
        edt_phone = findViewById(R.id.edt_phone);
        edit_company = findViewById(R.id.edit_company);
        btn_signup = findViewById(R.id.btn_signup);
        edt_email.setHint(getString(R.string.all_email)+" *");
        edit_pwd.setHint(getString(R.string.all_password)+" *");
        btn_signup.setOnClickListener(this);
        users = new Users();
    }

    @Override
    public void onToolbarLinstener() {

    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_sign_up;
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_signup:
                if(done()){
                    signUp();
                }
                break;
        }
    }

    private boolean done(){
        email = edt_email.getText().toString();
        pwd = edit_pwd.getText().toString();
        if(TextUtils.isEmpty(email)){
            String errorInfo = getString(R.string.invalid_email_msg);
            edt_email.setError(errorInfo);
            edt_email.requestFocus();
            return false;
        }
        if(TextUtils.isEmpty(pwd)){
            String errorInfo = getString(R.string.login_wrong_password_msg);
            edit_pwd.setError(errorInfo);
            edit_pwd.requestFocus();
            return false;
        }
        if(pwd.length() < 8){
            String errorInfo = getString(R.string.wrong_password_len_msg);
            edit_pwd.setError(errorInfo);
            edit_pwd.requestFocus();
            return false;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            String errorInfo = getString(R.string.login_email_wrong);
            edt_email.setError(errorInfo);
            edt_email.requestFocus();
            return false;
        } else if (TextUtils.isEmpty(pwd)) {
            String errorInfo = getString(R.string.login_wrong_password_msg);
            edit_pwd.setError(errorInfo);
            edit_pwd.requestFocus();
            return false;
        }
        return true;
    }

    private void signUp(){
        users.setUserName(email);
        pwd = CommonUtils.getSignature(pwd);
        users.setPassWord(pwd);
        users.setCompany(edit_company.getText().toString());
        users.setPhone(edt_phone.getText().toString());
        SignUpAPI.newInstance(this, users, new RetrofitCallback() {
            @Override
            public void onStart() {
                showProgressDialog("");
            }

            @Override
            public void onSuccess(Object result) {
                hideProgressDialog();
                Toast.makeText(SignUpActivity.this,"Sign up success",Toast.LENGTH_LONG).show();
                Intent intent = new Intent(SignUpActivity.this,LoginActivity.class);
                intent.putExtra("email",email);
                intent.putExtra("signUp",true);
                startActivity(intent);
                finish();
            }

            @Override
            public void onFailure(String errorMsg, int errorCode) {
                hideProgressDialog();
                if(errorMsg != null) {
                    Toast.makeText(SignUpActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }
        }).request();
    }
}