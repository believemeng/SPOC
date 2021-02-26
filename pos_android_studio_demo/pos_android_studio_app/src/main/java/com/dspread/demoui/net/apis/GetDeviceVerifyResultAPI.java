package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Time:2020/9/23
 * Author:Qianmeng Chen
 * Description:
 */
public final class GetDeviceVerifyResultAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/getDeviceStatus";
    private String nonce,jws;

    public static GetDeviceVerifyResultAPI newInstance(Context context, String nonce,String jws,RetrofitCallback callback){
        return new GetDeviceVerifyResultAPI(context,nonce,jws,callback);
    }

    private GetDeviceVerifyResultAPI(Context context,  String nonce,String jws,RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.nonce = nonce;
        this.jws = jws;
    }
    @Override
    protected RequestUtil getRequestParams() {
        RequestUtil requestUtil = RetrofitHttp.newPostRequest(fullUrl);
        JSONObject params = new JSONObject();
        try {
            params.put("nonce",nonce);
            params.put("jws",jws);
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
                callback.onSuccess(response);
            }
        });
    }
}
