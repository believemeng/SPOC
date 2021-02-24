package com.dspread.demoui.net;

import android.content.Context;

import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;
import com.dspread.demoui.utils.SPInstance;
import com.dspread.demoui.utils.TRACE;

/**
*@date:2020/8/21
*@author:Qianmeng Chen
*@description:网络框架的base api
*/
public abstract class RetrofitBaseAPI {
    protected RetrofitCallback callback;
    protected Context context;
    private String serverDomain;
    public String baseURL,fullUrl;

    public RetrofitBaseAPI(){

    }

    public RetrofitBaseAPI(Context context, RetrofitCallback callback,String url){
        this.context = context;
        this.callback = callback;
        this.fullUrl = url;
//        try {
//            serverDomain = SPInstance.getInstance(context).getServerDomain();
//        }catch (NullPointerException e){
//            serverDomain = null;
//        }
        buildURL();
    }

    private void buildURL() {
        if (serverDomain == null) {
            serverDomain = NetConstants.BaseUrl;
        }
        TRACE.w("==baseurl ="+serverDomain);
        baseURL = "https://" + serverDomain;
        if(RetrofitHttp.getBaseUrl() != null && !RetrofitHttp.getBaseUrl().isEmpty()){
            if(!RetrofitHttp.getBaseUrl().equals(baseURL)){
                RetrofitHttp.changeBaseUrl(baseURL);
            }
        }
        RetrofitHttp.init(context,baseURL);
    }

    protected abstract RequestUtil getRequestParams();

    //request the call
    public abstract void request();
}
