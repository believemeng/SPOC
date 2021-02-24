package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;
import com.dspread.demoui.utils.TRACE;

import org.json.JSONObject;

import retrofit2.Call;

/**
 * Time:2020/8/24
 * Author:Qianmeng Chen
 * Description:
 */
public class GetUserInfoAPI extends RetrofitBaseAPI {
//    https://spoc.dspread.com/api/monitor?phoneModel=huawei%20mate&systemVersion=10.0.11&appid=asmdsdsf3333&isRoot=true&lnt=116.2317&lat=39.5427
    public static final String RELATIVE_URL = "api/merchant/index";

    public static GetUserInfoAPI newInstance(Context context, RetrofitCallback callback){
        return new GetUserInfoAPI(context,callback);
    }

    private GetUserInfoAPI(Context context, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
    }
    @Override
    protected RequestUtil getRequestParams() {
        RequestUtil requestUtil = RetrofitHttp.newGetRequest(fullUrl);

        return requestUtil;
    }

    @Override
    public void request() {
        RetrofitHttp.getAsync(fullUrl, callback, new HttpResponseListener<Object>() {
            @Override
            public void onResponse(JSONObject response) {
                TRACE.i(response.toString());
            }
        });
//        Call call = RetrofitHttp.send(getRequestParams(), callback,new HttpResponseListener<Object>() {
//
//            @Override
//            public void onResponse(JSONObject t) {
//                //RetrofitHttp.updateToken(o.getValue().getToken());
//                //SPInstance.getInstance(context).saveTokenPack(o.getValue().getToken(), o.getValue().getExpiry(), true);
//                TRACE.i(t.toString());
//            }
//
//        });

    }
}
