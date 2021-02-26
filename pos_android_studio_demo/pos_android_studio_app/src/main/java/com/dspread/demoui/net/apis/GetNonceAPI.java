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
 * Time:2020/9/23
 * @author:Qianmeng Chen
 * Description:get the nonce
 */
public final class GetNonceAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/getRandomNumber";

    public static GetNonceAPI newInstance(Context context, RetrofitCallback callback){
        return new GetNonceAPI(context,callback);
    }

    private GetNonceAPI(Context context, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
    }

    @Override
    protected RequestUtil getRequestParams() {
        return null;
    }

    @Override
    public void request() {
        RetrofitHttp.getAsync(fullUrl, callback, new HttpResponseListener<Object>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int code = (int) response.get("code");
                    if(code == 200){
                        String data = (String) response.get("data");
                        callback.onSuccess(data);
                    }else{
                        callback.onFailure("Get Nonce null",code);
                    }
                } catch (JSONException e) {
//                    e.printStackTrace();
                }
            }
        });
    }
}
