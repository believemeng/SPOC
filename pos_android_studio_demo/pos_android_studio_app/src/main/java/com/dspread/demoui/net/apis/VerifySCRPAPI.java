package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.beans.PosInfos;
import com.dspread.demoui.net.RetrofitBaseAPI;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.net.retrofitUtil.RequestUtil;
import com.dspread.demoui.net.retrofitUtil.RetrofitHttp;
import com.dspread.demoui.utils.TRACE;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Qianmeng Chen
 */
public final class VerifySCRPAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "/api/verifySCRP";
    private PosInfos posInfos;

    public static VerifySCRPAPI newInstance(Context context, PosInfos posInfos, RetrofitCallback callback){
        return new VerifySCRPAPI(context,posInfos,callback);
    }

    private VerifySCRPAPI(Context context, PosInfos posInfos, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.posInfos = posInfos;
    }
    @Override
    protected RequestUtil getRequestParams() {
        RequestUtil requestUtil = RetrofitHttp.newPostRequest(fullUrl);
        JSONObject params = new JSONObject();
        try {
            params.put("terminalId",posInfos.getTerminalId());
            params.put("bootLoaderVersion",posInfos.getBootLoaderVersion());
            params.put("firmwareVersion",posInfos.getFirmwareVersion());
            params.put("hardwareVersion",posInfos.getHardwareVersion());
            params.put("PCIFirmwareVresion",posInfos.getPciFirmwareVresion());
            params.put("PCIHardwareVersion",posInfos.getPciHardwareVersion());
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
                TRACE.i("verify posinfo = "+response);
                callback.onSuccess(response);
            }
        });
    }
}
