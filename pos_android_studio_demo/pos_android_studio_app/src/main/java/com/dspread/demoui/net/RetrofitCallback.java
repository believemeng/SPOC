package com.dspread.demoui.net;

/**
 * Time:2020/8/21
 * Author:Qianmeng Chen
 * Description:网络请求的回调接口
 */
public interface RetrofitCallback<T> {
    void onStart();

    void onSuccess(T result);

    void onFailure(String errorMsg, int errorCode);
}
