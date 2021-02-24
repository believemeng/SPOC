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
 * Time:2020/9/15
 * Author:Qianmeng Chen
 * Description:
 */
public class GetDevicesVerifyTokenAPI extends RetrofitBaseAPI {

    public static final String RELATIVE_URL = "/api/envelop";
    private String deviceInfo;

    public static GetDevicesVerifyTokenAPI newInstance(Context context, String deviceInfo, RetrofitCallback callback){
        return new GetDevicesVerifyTokenAPI(context,deviceInfo,callback);
    }

    private GetDevicesVerifyTokenAPI(Context context, String deviceInfo, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.deviceInfo = deviceInfo;
    }

    @Override
    protected RequestUtil getRequestParams() {
        return null;
    }

    @Override
    public void request() {
        RetrofitHttp.postStr(fullUrl, deviceInfo, callback, new HttpResponseListener<Object>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    int code = (int) response.get("code");
                    if(code == 200){
                        String data = (String) response.get("data");
                        callback.onSuccess(data);
                    }else{
                        callback.onFailure("Active Device Fail!",code);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
