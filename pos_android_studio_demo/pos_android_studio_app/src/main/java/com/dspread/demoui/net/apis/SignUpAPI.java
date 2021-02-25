package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.beans.Users;
import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Time:2020/9/10
 * Author:Qianmeng Chen
 * Description:
 */
public final class SignUpAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/merchant/regist";
    private Users users;

    public static SignUpAPI newInstance(Context context, Users users, RetrofitCallback callback){
        return new SignUpAPI(context,users,callback);
    }

    private SignUpAPI(Context context, Users users, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.users = users;
    }
    @Override
    protected RequestUtil getRequestParams() {
        RequestUtil requestUtil = RetrofitHttp.newPostRequest(fullUrl);
        JSONObject params = new JSONObject();
        try {
            params.put("username",users.getUserName());
            params.put("password",users.getPassWord());
            params.put("company",users.getCompany());
            params.put("phone",users.getPhone());
            requestUtil.putParamsObj(params);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return requestUtil;
    }

    @Override
    public void request() {
        RetrofitHttp.send(getRequestParams(), callback, new HttpResponseListener<Object>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int code = (int) response.get("code");
                    String msg = (String) response.get("msg");
                    if(code == 200){
                        callback.onSuccess(response);
                    }else{
                        callback.onFailure(msg,code);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });
    }
}
