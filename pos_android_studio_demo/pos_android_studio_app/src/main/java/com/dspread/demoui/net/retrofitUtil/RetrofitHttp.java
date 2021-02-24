package com.dspread.demoui.net.retrofitUtil;

import android.content.Context;

import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;

import retrofit2.Call;

/**
*@date:2020/8/21
*@author:Qianmeng Chen
*@description:封装的可对外直接调用的网络接口
*/
public class RetrofitHttp {

    private static Context mContext;
    public static Context getContext() {
        return mContext;
    }

    public static void setHttpCache(boolean cache){
        HttpHelper.setHttpCache(cache);
    }

    public static void init(Context context, String httpBaseUrl) {
        mContext = context;
        HttpHelper.setBaseUrl(httpBaseUrl);
    }

    public static String getBaseUrl(){
        return HttpHelper.getBaseUrl();
    }

    public static <T> Call getAsync(String apiUrl, RetrofitCallback callback, final HttpResponseListener<T> httpResponseListener) {
        callback.onStart();
        return HttpHelper.getAsync(mContext,apiUrl, callback,null, httpResponseListener);
    }

    public static <T> Call downloadFileAsyc(String apiUrl, RetrofitCallback callback, final HttpResponseListener<T> httpResponseListener) {
        callback.onStart();
        return HttpHelper.getFileAsync(mContext,apiUrl, callback,null, httpResponseListener);
    }

    public static <T> Call getStr(String apiUrl, String param,RetrofitCallback callback, final HttpResponseListener<T> httpResponseListener) {
        callback.onStart();
        return HttpHelper.getStr(mContext,apiUrl, callback,param, httpResponseListener);
    }

    public static <T> Call postStr(String apiUrl, String param, RetrofitCallback callback, final HttpResponseListener<T> httpResponseListener) {
        callback.onStart();
        return HttpHelper.postStr(mContext,apiUrl, callback,param, httpResponseListener);
    }

    public static <T> Call postAsync(String apiUrl, RetrofitCallback callback,HttpResponseListener<T> httpResponseListener) {
        callback.onStart();
        return HttpHelper.postAsync(apiUrl, callback,null,  httpResponseListener);
    }

    public static <T> Call putAsync(String apiUrl,RetrofitCallback callback,HttpResponseListener<T> httpResponseListener){
        callback.onStart();
        return HttpHelper.putAsync(apiUrl,callback,null,httpResponseListener);
    }

    public static <T> Call deleteAsync(String apiUrl,RetrofitCallback callback,HttpResponseListener<T> httpResponseListener){
        callback.onStart();
        return HttpHelper.deleteAsync(apiUrl,callback,null,httpResponseListener);
    }

    public static <T> Call upload(RequestUtil request,RetrofitCallback callback,HttpResponseListener<T> httpResponseListener){
        callback.onStart();
        return HttpHelper.uploadObjAsync(request.getApiUlr(),request.getFile(),callback,request.getObjParams(),httpResponseListener);
    }

    public static <T> Call uploadAsync(RequestUtil request,RetrofitCallback callback,HttpResponseListener<T> httpResponseListener){
        callback.onStart();
        return HttpHelper.uploadAsync(request.getApiUlr(),request.getFile(),callback,null,httpResponseListener);
    }

    public static void changeBaseUrl(String baseUrl){
        HttpHelper.getInstance().changeApiBaseUrl(baseUrl);
    }

    public static String getFileType(String fileName) {
        try {
            return fileName.substring(fileName.lastIndexOf("."), fileName.length());
        }catch (Exception e){
            return "";
        }
    }

    public static void updateToken(String token,long tokenExpiryDate,boolean isLogin){
        HttpHelper.getInstance().changeToken(token,tokenExpiryDate,isLogin);
    }

    public static void updateToken(String token,boolean isLogin){
        HttpHelper.getInstance().changeToken(token,0,isLogin);
    }

    public static <T> Call sendArr(RequestUtil request, RetrofitCallback callback,HttpResponseListener<T> httpResponseListener) {
        callback.onStart();
        if(RequestMethod.PUT.equals(request.getRequestMethod())){
            return HttpHelper.putArrAsync(request.getApiUlr()
                    ,callback
                    , request.getArrayParams()
                    , httpResponseListener);
        }else if(RequestMethod.DELETE.equals(request.getRequestMethod())){
            return HttpHelper.deleteArrAsync(request.getApiUlr()
                    ,callback
                    , request.getArrayParams()
                    , httpResponseListener);
        }else{
            return HttpHelper.postArrAsync(mContext,request.getApiUlr()
                    ,callback
                    , request.getArrayParams()
                    , httpResponseListener);
        }
    }

    /**
     * 发送http网络请求
     *
     * @param request
     * @param httpResponseListener
     * @param <T>
     * @return
     */
    public static <T> Call send(RequestUtil request, RetrofitCallback callback,HttpResponseListener<T> httpResponseListener) {
        callback.onStart();
        if (RequestMethod.GET.equals(request.getRequestMethod())) {
            return HttpHelper.getObjAsync(request.getApiUlr()
                    ,callback
                    , request.getObjParams()
                    , httpResponseListener);
        } else if(RequestMethod.PUT.equals(request.getRequestMethod())){
            return HttpHelper.putObjAsync(request.getApiUlr()
                    ,callback
                    , request.getObjParams()
                    , httpResponseListener);
        }else if(RequestMethod.DELETE.equals(request.getRequestMethod())){
            return HttpHelper.deleteObjAsync(request.getApiUlr()
                    ,callback
                    , request.getObjParams()
                    , httpResponseListener);
        }else{
            //return HttpHelper.postAsync(request.getApiUlr()
            //        , request.getHeaderMap()
            //        , request.getParamsMap()
            //        , httpResponseListener);
            return HttpHelper.postObjAsync(mContext,request.getApiUlr()
                    ,callback
                    , request.getObjParams()
                    , httpResponseListener);
        }
    }

    /**
     * @param apiUlr 格式：xxxx/xxxxx
     * @return
     */
    public static RequestUtil newRequest(String apiUlr, RequestMethod method) {
        return new RequestUtil(apiUlr, method);
    }

    /**
     * @param apiUlr 格式：xxxx/xxxxx
     * @return
     */
    public static RequestUtil newPostRequest(String apiUlr) {
        return new RequestUtil(apiUlr, RequestMethod.POST);
    }

    /**
     * @param uploadFileUrl 格式：http://xxxx/xxxxx
     * @return
     */
    public static RequestUtil newUploadRequest(String uploadFileUrl, RequestMethod method) {
        return new RequestUtil(uploadFileUrl, method);
    }

    /**
     * 默认是GET方式
     *
     * @param apiUlr 格式：xxxx/xxxxx
     * @return
     */
    public static RequestUtil newGetRequest(String apiUlr) {
        return new RequestUtil(apiUlr, RequestMethod.GET);
    }

}
