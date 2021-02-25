package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.beans.PhoneInfos;
import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;

import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author Qianmeng Chen
 * @version 1.0
 * @date 2020-10-16
 */
public final class VerifyAppAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/verifyAppSignature";
    private String signature;

    public static VerifyAppAPI newInstance(Context context, String signature, RetrofitCallback callback){
        return new VerifyAppAPI(context,signature,callback);
    }

    private VerifyAppAPI(Context context, String signature, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.signature = signature;
    }

    @Override
    protected RequestUtil getRequestParams() {
        RequestUtil requestUtil = RetrofitHttp.newPostRequest(fullUrl);
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("appSignature",signature);
            requestUtil.putParamsObj(jsonObject);
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
                    if(((Integer) response.get("code")) == 200){
                        callback.onSuccess(true);
                    }else {
                        callback.onSuccess(false);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
