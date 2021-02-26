package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;
import com.dspread.demoui.utils.TRACE;

import org.json.JSONObject;

public final class LogoutAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/merchant/logout";
    private String userName,pwd;

    public static LogoutAPI newInstance(Context context,  RetrofitCallback callback){
        return new LogoutAPI(context,callback);
    }

    private LogoutAPI(Context context, RetrofitCallback callback){
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
                TRACE.i("logout "+response);
                callback.onSuccess(response);
            }
        });
    }
}
