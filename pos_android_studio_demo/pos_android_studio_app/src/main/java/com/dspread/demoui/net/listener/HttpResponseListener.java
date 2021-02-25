package com.dspread.demoui.net.listener;


import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * HttpResponseListener
 */

public abstract class HttpResponseListener<T> {

    public Type getType() {
        Type mySuperClass = getClazz().getGenericSuperclass();
        Type type = ((ParameterizedType) mySuperClass).getActualTypeArguments()[0];
        return type;
    }

    public Class getClazz() {
        return getClass();
    }

    //public abstract void onResponse(T t, Headers headers);

    public abstract void onResponse(JSONObject response);

    public abstract void onResponse(ResponseBody responseBody) ;

    public abstract void onFailure(Call<ResponseBody> call, Throwable e);

    public abstract void onFailure(String msg,int errorCode);
}
