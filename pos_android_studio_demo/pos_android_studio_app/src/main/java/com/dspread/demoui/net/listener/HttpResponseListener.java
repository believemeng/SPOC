package com.dspread.demoui.net.listener;


import org.json.JSONObject;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import okhttp3.ResponseBody;
import retrofit2.Call;

/**
 * HttpResponseListener
 */
// file deepcode ignore ClassWithOnlyPrivateConstructorsShouldBeFinal: <comment the reason here>
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

    public void onResponse(ResponseBody responseBody){} ;

    public void onFailure(Call<ResponseBody> call, Throwable e){};

    public void onFailure(String msg,int errorCode){};
}
