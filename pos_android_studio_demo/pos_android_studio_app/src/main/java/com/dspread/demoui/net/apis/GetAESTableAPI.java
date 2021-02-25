package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.beans.PosInfos;
import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;
import com.dspread.demoui.utils.TRACE;

import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import okhttp3.ResponseBody;

public final class GetAESTableAPI extends RetrofitBaseAPI {

    public static final String RELATIVE_URL = "/api/generateAESTable";

    public static GetAESTableAPI newInstance(Context context, RetrofitCallback callback){
        return new GetAESTableAPI(context,callback);
    }

    private GetAESTableAPI(Context context, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
    }

    @Override
    protected RequestUtil getRequestParams() {
        return null;
    }

    @Override
    public void request() {
        RetrofitHttp.downloadFileAsyc(fullUrl, callback, new HttpResponseListener<Object>() {
            @Override
            public void onResponse(JSONObject response) {

            }

            @Override
            public void onResponse(ResponseBody response) {
                TRACE.i("aes table = "+response.toString());
                try {
                    callback.onSuccess(response.bytes());
                } catch (IOException e) {
                    //e.printStackTrace();
                }
            }
        });
    }
}
