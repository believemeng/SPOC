package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;
import com.dspread.demoui.utils.SPInstance;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Time:2020/9/10
 * Author:Qianmeng Chen
 * Description:
 */
public final class LoginAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/merchant/login";
    private String userName,pwd;

    public static LoginAPI newInstance(Context context, String userName,String pwd,RetrofitCallback callback){
        return new LoginAPI(context,userName,pwd,callback);
    }

    private LoginAPI(Context context, String userName,String pwd, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.userName = userName;
        this.pwd = pwd;
    }
    @Override
    protected RequestUtil getRequestParams() {
        RequestUtil requestUtil = RetrofitHttp.newPostRequest(fullUrl);
        JSONObject params = new JSONObject();
        try {
            params.put("username",userName);
            params.put("password",pwd);
            requestUtil.putParamsObj(params);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return requestUtil;
    }

    @Override
    public void request() {
        RetrofitHttp.send(getRequestParams(), callback, new HttpResponseListener<Object>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    String code = (String) response.get("code");
                    String msg = (String) response.get("msg");
                    String token = (String) response.get("data");
                    SPInstance.getInstance(context).saveToken(token);
                    if(code.equals("200")){
                        callback.onSuccess(response);
                    }else{
                        callback.onFailure(msg,Integer.valueOf(code));
                    }
                } catch (JSONException e) {
                    //e.printStackTrace();
                }
            }
        });
    }
}
