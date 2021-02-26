package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.beans.PhoneInfos;
import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;

/**
 * Time:2020/9/17
 * Author:Qianmeng Chen
 * Description:
 */
public class GetDeviceStatusAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/getDeviceStatus";
    private PhoneInfos phoneInfos;

    public static GetDeviceStatusAPI newInstance(Context context, PhoneInfos phoneInfos, RetrofitCallback callback){
        return new GetDeviceStatusAPI(context,phoneInfos,callback);
    }

    private GetDeviceStatusAPI(Context context, PhoneInfos phoneInfos, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.phoneInfos = phoneInfos;
    }

    @Override
    protected RequestUtil getRequestParams() {
        return null;
    }

    @Override
    public void request() {

    }
}
