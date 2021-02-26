package com.dspread.demoui.net.apis;

import android.content.Context;

import com.dspread.demoui.beans.Transactions;
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
public class UploadTransactionAPI extends RetrofitBaseAPI {
    public static final String RELATIVE_URL = "api/transaction";
    private Transactions transactions;

    public static UploadTransactionAPI newInstance(Context context,Transactions transactions, RetrofitCallback callback){
        return new UploadTransactionAPI(context,transactions,callback);
    }

    private UploadTransactionAPI(Context context,Transactions transactions, RetrofitCallback callback){
        super(context,callback,RELATIVE_URL);
        this.transactions = transactions;
    }
    @Override
    protected RequestUtil getRequestParams() {
        RequestUtil requestUtil = RetrofitHttp.newPostRequest(fullUrl);
        JSONObject params = new JSONObject();
        try {
            params.put("holderData",transactions.getHolderData());
            params.put("pin",transactions.getPin());
            params.put("amount",transactions.getAmount());
            params.put("tranTime",transactions.getTranTime());
            requestUtil.putParamsObj(params);
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
                TRACE.w("upload trade = "+response);
            }
        });
    }
}
