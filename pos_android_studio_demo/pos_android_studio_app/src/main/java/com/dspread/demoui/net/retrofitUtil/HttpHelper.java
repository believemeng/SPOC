package com.dspread.demoui.net.retrofitUtil;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.widget.Toast;

import com.dspread.demoui.R;
import com.dspread.demoui.net.ApiService;
import com.dspread.demoui.net.RetrofitCallback;
import com.dspread.demoui.net.listener.HttpResponseListener;
import com.dspread.demoui.utils.PackageUtils;
import com.dspread.demoui.utils.SPInstance;
import com.dspread.demoui.utils.TRACE;
import com.google.gson.Gson;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
*@date:2020/8/21
*@author:Qianmeng Chen
*@description:
*/
public final class HttpHelper {
    private static WeakReference<HttpHelper> sInstance;
    private volatile Retrofit mRetrofit;
    public  static Map<String, Object> mHeaderMap;

    private static String sBaseUrl;
    public static final String MAC_ALGORITHM = "HMACSHA256";
    private static OkHttpClient okHttpClient;
    private OkHttpClient.Builder builder;
    private final long TIMEOUT_DURATION = 600;
    private String keyStorePwd = "Dspread2020";

    private File mCacheFile;
    //默认支持http缓存
    private static boolean mIsCache = true;

    public static void setBaseUrl(String baseUrl) {
        sBaseUrl = baseUrl;
    }

    public static String getsBaseUrl(){
        return sBaseUrl;
    }

    /**
     * 设置是否缓存http请求数据
     *
     * @param isCache
     */
    public static void setHttpCache(boolean isCache) {
        mIsCache = isCache;
    }


    public static String getBaseUrl() {
        return sBaseUrl;
    }

    private HttpHelper() {
        //缓存路径
        mCacheFile = new File(RetrofitHttp.getContext().getCacheDir().getAbsolutePath() + File.separator + "retrofit2_http_cache");
        //判断缓存路径是否存在
        if (!mCacheFile.exists() && !mCacheFile.isDirectory()) {
            mCacheFile.mkdir();
        }

        okHttpClient = createOkhttpAndCache();
        try {
            if (sBaseUrl != null && !sBaseUrl.equals("") && !sBaseUrl.isEmpty()) {
                mRetrofit = new Retrofit.Builder()
                        .baseUrl(sBaseUrl)
                        .client(okHttpClient)
                        .addConverterFactory(StringConverterFactory.create())
                        .build();
            }
        }catch (Exception e){

        }
    }

    public void changeApiBaseUrl(String newApiBaseUrl) {
        mRetrofit = new Retrofit.Builder().baseUrl(newApiBaseUrl)
                .addConverterFactory(StringConverterFactory.create())
                .client(okHttpClient)
                .build();
    }

    public void changeToken(final String token, final long tokenExpiryDate, final boolean isLogin){
        if(builder != null){
            builder.addInterceptor(new Interceptor() {
                @Override
                public okhttp3.Response intercept(Chain chain) throws IOException {
                    Request original = chain.request();
//                    Request.Builder requestBuilder = original.newBuilder()
//                            .header("token", token)
//                            .header("token", SPInstance.getInstance(RetrofitHttp.getContext()).getToken());
                    Request.Builder requestBuilder = original.newBuilder()
                            .header("token", token);
                    Request request = requestBuilder.build();
//                    if(tokenExpiryDate != 0){
//                        SPInstance.getInstance(RetrofitHttp.getContext()).saveTokenPack(token,tokenExpiryDate,isLogin);
//                    }else{
//                        long time = SPInstance.getInstance(RetrofitHttp.getContext()).getTokenExpiryDate();
//                        SPInstance.getInstance(RetrofitHttp.getContext()).saveTokenPack(token,time,isLogin);
//
//                    }
                    return chain.proceed(request);
                }
            });
        }
    }

    public static String getAuth() {
        String auth = "";
        try {
            StringBuffer hash = new StringBuffer();
            Mac mac = Mac.getInstance(MAC_ALGORITHM);
            SecretKey secret = new SecretKeySpec(
                    "MF0CAQACEACapObTvEGQiZDYQBqelTMCAwEAAQIPdOAqYF5IgxzYSKC3fPZpAggMqmARm07uBwIIDDW4ck6i1HUCCAXIjtG7X5FlAggIgZBrCf+EVQIICGZtS4L2phc=" // 3.8.0
                            .getBytes(), MAC_ALGORITHM);
            mac.init(secret);
            long time = System.currentTimeMillis();
            //            String data = HTTP_REQUEST_TYPE_ARRAY[requestMethod] + "android_connect" + PackageUtils.getVersion(MyApplication.getInst()) + String.valueOf(time);
            String data = "android" + "3.8" + String.valueOf(time);
            byte[] doFinal = mac.doFinal(data.getBytes());
            for (int i = 0; i < doFinal.length; i++) {
                String hex = Integer.toHexString(0xFF & doFinal[i]);
                if (hex.length() == 1) {
                    hash.append('0');
                }
                hash.append(hex);
            }
            String ret = hash.toString();
            auth = "d=" + ret + ";" + "v=" + PackageUtils.getVersion(RetrofitHttp.getContext()) + ";k=android;ts=" + String.valueOf(time);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        }
        return auth;
    }

    public static Map getHeaders(Context context) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", SPInstance.getInstance(context).getToken());
        //        headers.put("token", "error");
        headers.put("User-Agent", "cotsModel:Android;cotsVersion:8.0");
        headers.put("TerminalId",SPInstance.getInstance(context).getTerminalId());
        return headers;
    }

    /**
     * 创建okhttp & 添加缓存
     *
     * @return
     */
    private OkHttpClient createOkhttpAndCache() {
        builder = new OkHttpClient.Builder();
        if (mIsCache) {
            //http缓存大小10M
            Cache cache = new Cache(mCacheFile, 1024 * 1024 * 10);
            //添加网络过滤 & 实现网络数据缓存
            Interceptor interceptor = createHttpInterceptor();
            builder.addNetworkInterceptor(interceptor);
            builder.addInterceptor(interceptor);
            try {
                HttpsUtil.SSLParams sslParams = HttpsUtil.getSslSocketFactory(null, RetrofitHttp.getContext().getAssets().open("client.bks"), keyStorePwd);
                builder.sslSocketFactory(sslParams.sSLSocketFactory, sslParams.trustManager);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //给okhttp添加缓存
            builder.cache(cache);
        }
        //设置超时

        builder.connectTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS);
        builder.readTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS);
        builder.writeTimeout(TIMEOUT_DURATION, TimeUnit.SECONDS);
        builder.addInterceptor(new LoggingInterceptor());
//        builder.addInterceptor(new TokenInterceptor());
        builder.addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                String token = SPInstance.getInstance(RetrofitHttp.getContext()).getToken();
                Request.Builder requestBuilder = original.newBuilder()
                        .header("Authorization", SPInstance.getInstance(RetrofitHttp.getContext()).getToken());//令牌;
                Request request = requestBuilder.build();
                return chain.proceed(request);
            }
        });
        //错误重连
        builder.retryOnConnectionFailure(true);
        return builder.build();
    }

    public static HttpHelper getInstance() {
        if (sInstance == null || sInstance.get() == null) {
            synchronized (HttpHelper.class) {
                if (sInstance == null || sInstance.get() == null) {
                    sInstance = new WeakReference<HttpHelper>(new HttpHelper());
                }
            }
        }
        return sInstance.get();
    }

    public static <T> Call getAsync(Context context, String apiUrl, RetrofitCallback callback, Map<String, Object> paramMap, final HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        Call<ResponseBody> call = httpService.get(getHeaders(context),apiUrl, paramMap);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call getFileAsync(Context context, String apiUrl, RetrofitCallback callback, Map<String, Object> paramMap, final HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        Call<ResponseBody> call = httpService.get(getHeaders(context),apiUrl, paramMap);
        downloadFile(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call getStr(Context context, String apiUrl, RetrofitCallback callback, String param, final HttpResponseListener<T> httpResponseListener) {
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), param);
        Call<ResponseBody> call = httpService.getStr(getHeaders(context),apiUrl, body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call postStr(Context context, String apiUrl, RetrofitCallback callback, String param, final HttpResponseListener<T> httpResponseListener) {
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), param);
        Call<ResponseBody> call = httpService.postStr(getHeaders(context),apiUrl, body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call putAsync(String apiUrl,RetrofitCallback callback,  Map<String, Object> paramMap, final HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        Call<ResponseBody> call = httpService.put(apiUrl, paramMap);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call deleteAsync(String apiUrl,RetrofitCallback callback,  Map<String, Object> paramMap, final HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        Call<ResponseBody> call = httpService.delete(apiUrl, paramMap);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call getObjAsync(String apiUrl, RetrofitCallback callback, JSONObject paramMap, final HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new JSONObject();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
        Call<ResponseBody> call = httpService.get(apiUrl, body);
        parseNetData(call,callback, httpResponseListener);
        return call;
    }

    public static <T> Call postAsync(String apiUrl, RetrofitCallback callback, Map<String, Object> paramMap, HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new HashMap<>();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        Call<ResponseBody> call = httpService.post(apiUrl, paramMap);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call uploadAsync(String apiUrl, File file,RetrofitCallback callback, Map<String, Object> paramMap, HttpResponseListener<T> httpResponseListener) {
//        if (paramMap == null) {
////            paramMap = new HashMap<>();
//        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody requestBody = RequestBody.create(RequestUtil.getMediaType(), file);
        MultipartBody.Part body;
        try {
            body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        }catch (IllegalArgumentException e){
            body = MultipartBody.Part.createFormData("file", URLEncoder.encode(file.getName()), requestBody);
        }
        Call<ResponseBody> call = httpService.upload(apiUrl, body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }


    public static <T> Call uploadObjAsync(String apiUrl,File file,RetrofitCallback callback,  JSONObject paramMap, final HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new JSONObject();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody jsonBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), new Gson().toJson(paramMap));
        RequestBody requestBody = RequestBody.create(RequestUtil.getMediaType(), file);
        MultipartBody.Part body;
        try {
            body = MultipartBody.Part.createFormData("file", file.getName(), requestBody);
        }catch (IllegalArgumentException e){
            body = MultipartBody.Part.createFormData("file", URLEncoder.encode(file.getName()), requestBody);
        }
        Call<ResponseBody> call = httpService.upload(apiUrl, jsonBody,body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call postObjAsync(Context context,String apiUrl, RetrofitCallback callback, JSONObject paramMap, HttpResponseListener<T> httpResponseListener) {
        if (paramMap == null) {
            paramMap = new JSONObject();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
        Call<ResponseBody> call = httpService.postBody(getHeaders(context),apiUrl,body);
        parseNetData(call,callback, httpResponseListener);
        return call;
    }

//    public static <T> Call postObjAsync(Context context,String apiUrl, RetrofitCallback callback, Map<String, Object> paramMap, HttpResponseListener<T> httpResponseListener) {
//        if (paramMap == null) {
//            paramMap = new HashMap<>();
//        }
//        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
//        Call<ResponseBody> call = httpService.post(apiUrl, paramMap);
//        parseNetData(call,callback, httpResponseListener);
//        return call;
//    }

    public static void getHeadersMap(Context context){
        Map<String,String> headers = new HashMap<>();
        TRACE.i("org limit token = "+SPInstance.getInstance(context).getToken());
        headers.put("token",SPInstance.getInstance(context).getToken());
        headers.put("User-Agent",getAuth());
    }

    public static <T> Call putObjAsync(String apiUrl,RetrofitCallback callback,JSONObject paramMap,HttpResponseListener<T> httpResponseListener){
        if (paramMap == null) {
            paramMap = new JSONObject();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
        Call<ResponseBody> call = httpService.put(apiUrl,body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call putArrAsync(String apiUrl, RetrofitCallback callback, JSONArray paramMap, HttpResponseListener<T> httpResponseListener){
        if (paramMap == null) {
            paramMap = new JSONArray();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
        Call<ResponseBody> call = httpService.put(apiUrl,body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call postArrAsync(Context context,String apiUrl, RetrofitCallback callback, JSONArray paramMap, HttpResponseListener<T> httpResponseListener){
        if (paramMap == null) {
            paramMap = new JSONArray();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
        Call<ResponseBody> call = httpService.postBody(getHeaders(context),apiUrl,body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call deleteArrAsync(String apiUrl, RetrofitCallback callback, JSONArray paramMap, HttpResponseListener<T> httpResponseListener){
        if (paramMap == null) {
            paramMap = new JSONArray();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
        Call<ResponseBody> call = httpService.delete(apiUrl,body);
        parseNetData(call, callback,httpResponseListener);
        return call;
    }

    public static <T> Call deleteObjAsync(String apiUrl,RetrofitCallback callback,JSONObject paramMap,HttpResponseListener<T> httpResponseListener){
        if (paramMap == null) {
            paramMap = new JSONObject();
        }
        ApiService httpService = getInstance().mRetrofit.create(ApiService.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
        Call<ResponseBody> call = httpService.delete(apiUrl,body);
        parseNetData(call,callback, httpResponseListener);
        return call;
    }

    private static RetrofitCallback mcallback;
    private static boolean isTokenExpired;
    private static <T> void parseNetData(Call<ResponseBody> call, final RetrofitCallback callback, final HttpResponseListener<T> httpResponseListener) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                   TRACE.i("response msg:" + response.message() +" "+response.code());
                    mcallback = callback;
                    String errorMsg = "";
                    if(response.body() != null) {
                        String json = response.body().string();
                        TRACE.i("json ="+json);
//                        if (L.isDebug) {
                            //L.i("response data:" + json);
//                        }
                        //这个用gson解析，先不用
                        JSONObject jsonObject = null;
                        if(json.contains("code")){
                            jsonObject = new JSONObject(json);
                            String code = jsonObject.getString("code");
                            if(code.equals("401")){
                                RetrofitAuthUtil.showReLogoutDialog(RetrofitHttp.getContext(),"",RetrofitHttp.getContext().getString(R.string.token_expr_error_msg));
                                return;
                            }
                        }else {
                            httpResponseListener.onResponse(response.body());
                            return;
                        }
                        if(json.contains("token")) {
                            if (jsonObject.getString("token") != null) {
                                String token = jsonObject.getString("token");
                                SPInstance.getInstance(RetrofitHttp.getContext()).saveToken(token);
                            }
                        }
                        httpResponseListener.onResponse(new JSONObject(json));

                    }else{
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        JSONObject errorObj = jsonObject.optJSONArray("errors").optJSONObject(0);
                        int code = errorObj.optInt("code");
                        httpResponseListener.onFailure(errorMsg,code);
                        switch (code) {
                            case 401:
                                //expired
                                isTokenExpired = true;
                                RetrofitAuthUtil.showReLogoutDialog(RetrofitHttp.getContext(),"",RetrofitHttp.getContext().getString(R.string.token_expr_error_msg));
                                break;
//                            case TICKET_SOLD_OUT:
//                                VolleyAuth.showApiErrorDialog(RetrofitHttp.getContext(),RetrofitHttp.getContext().getString(R.string.all_error),RetrofitHttp.getContext().getString(R.string.error_ticket_sold_out_msg));
//                                break;
//                            case ATTENDEE_HAS_PAID:
//                                VolleyAuth.showApiErrorDialog(RetrofitHttp.getContext(),RetrofitHttp.getContext().getString(R.string.all_error),RetrofitHttp.getContext().getString(R.string.error_attendee_has_paid_msg));
//                                break;
//                            case INVALID_FAPIAO_INFORMATION:
//                                VolleyAuth.showApiErrorDialog(RetrofitHttp.getContext(),RetrofitHttp.getContext().getString(R.string.all_error),RetrofitHttp.getContext().getString(R.string.error_invalid_fapiao_information_msg));
//                                break;
                        }
                        callback.onFailure(errorMsg,code);
                        TRACE.i("response error body:" + response.errorBody().string());

                    }
                } catch (Exception e) {
                    httpResponseListener.onFailure(call, e);
                    callback.onFailure("",0);
                   if(!isNetworkConnected(RetrofitHttp.getContext())){
                       Toast.makeText(RetrofitHttp.getContext(), RetrofitHttp.getContext().getString(R.string.no_network_msg), Toast.LENGTH_LONG).show();
                   }
//                    else if(e instanceof NoConnectionError){
//                        Toast.makeText(RetrofitHttp.getContext(), RetrofitHttp.getContext().getString(R.string.no_network_msg), Toast.LENGTH_LONG).show();
//                    }else if(e instanceof TimeoutError){
//                        Toast.makeText(RetrofitHttp.getContext(), RetrofitHttp.getContext().getString(R.string.time_out), Toast.LENGTH_LONG).show();
//                    }else if(e instanceof JSONException){
//                       TRACE.w("Http Exception:"+ e);
//                    }else {
////                       Toast.makeText(RetrofitHttp.getContext(), RetrofitHttp.getContext().getString(R.string.error_connection_timeout), Toast.LENGTH_LONG).show();
//                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                httpResponseListener.onFailure(call, t);
//                if(t instanceof NoConnectionError){
//                    Toast.makeText(RetrofitHttp.getContext(), RetrofitHttp.getContext().getString(R.string.no_network_msg), Toast.LENGTH_LONG).show();
//                }else if(t instanceof TimeoutError){
//                    Toast.makeText(RetrofitHttp.getContext(), RetrofitHttp.getContext().getString(R.string.time_out), Toast.LENGTH_LONG).show();
//                }else{
////                    Toast.makeText(RetrofitHttp.getContext(), RetrofitHttp.getContext().getString(R.string.error_connection_timeout), Toast.LENGTH_LONG).show();
//                }
            }

        });
    }

    private static <T> void downloadFile(Call<ResponseBody> call, final RetrofitCallback callback, final HttpResponseListener<T> httpResponseListener) {
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    TRACE.i("response msg:" + response.message() +" "+response.code());
                    mcallback = callback;
                    String errorMsg = "";
                    if(response.body() != null) {
                        httpResponseListener.onResponse(response.body());
                    }else{
                        JSONObject jsonObject = new JSONObject(response.errorBody().string());
                        JSONObject errorObj = jsonObject.optJSONArray("errors").optJSONObject(0);
                        int code = errorObj.optInt("code");
                        httpResponseListener.onFailure(errorMsg,code);
                    }
                } catch (Exception e) {
                    httpResponseListener.onFailure(call, e);
                    callback.onFailure("",0);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                httpResponseListener.onFailure(call, t);
            }

        });
    }

    /**
     * 创建网络过滤对象，添加缓存
     *
     * @return
     */
    private Interceptor createHttpInterceptor() {
        return new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request request = chain.request();

                boolean isNetwork = isNetworkConnected(RetrofitHttp.getContext());

                //没有网络情况下，仅仅使用缓存（CacheControl.FORCE_CACHE;）
                if (!isNetwork) {
                    request = request.newBuilder()
                            .cacheControl(CacheControl.FORCE_CACHE)
                            .build();
                }
                okhttp3.Response response = chain.proceed(request);
                if (isNetwork) {
                    int maxAge = 0;
                    // 有网络时, 不缓存, 最大保存时长为0
                    response.newBuilder()
                            .header("Cache-Control", "public, max-age=" + maxAge)
                            .removeHeader("Pragma")
                            .build();
                } else {
                    // 无网络时，设置超时为4周
                    int maxStale = 60 * 60 * 24 * 28;
                    response.newBuilder()
                            .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                            .removeHeader("Pragma")
                            .build();
                }
                return response;
            }
        };
    }

    /**
     * 文件下载回调
     */
    public interface DownloadHandler {
        /**
         * 接收到数据体
         * @param body 响应体
         */
        void onBody(ResponseBody body);

        /**
         * 文件下载出错
         */
        void onError();
    }

    /**
     * 判断是否有网络连接
     *
     * @param context
     * @return
     */
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
            if (mNetworkInfo != null) {
                return mNetworkInfo.isAvailable();
            }
        }
        return false;
    }

    /**
     * 拦截器，显示日志信息
     */
    public class LoggingInterceptor implements Interceptor {
        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {

            //这个chain里面包含了request和response，所以你要什么都可以从这里拿
            Request request = chain.request();

            long t1 = System.nanoTime();
            //请求发起的时间

            TRACE.i(String.format("发送请求 %s on %s%n%s",request.url(), chain.connection(), request.headers()));

            okhttp3.Response response = chain.proceed(request);

            long t2 = System.nanoTime();
            //收到响应的时间

            //这里不能直接使用response.body().string()的方式输出日志
            //因为response.body().string()之后，response中的流会被关闭，程序会报错，我们需要创建出一
            //个新的response给应用层处理
            long time = TimeUnit.SECONDS.convert((t2-t1),TimeUnit.NANOSECONDS);
            if(time > TIMEOUT_DURATION){
                Looper.prepare();
//                ToastUtils.Tlong(RetrofitHttp.getContext(),RetrofitHttp.getContext().getString(R.string.error_connection_timeout));
                Looper.loop();
                return response;
            }
            ResponseBody responseBody = response.peekBody(1024 * 1024);
            TRACE.i(String.format("接收响应: [%s] %n返回json:【%s】 %.1fms%n%s",
                    response.request().url(),
                    responseBody.string(),
                    (t2 - t1) / 1e6d,
                    response.headers()));
            return response;
        }
    }

    //token 过期的拦截器
    public class TokenInterceptor implements Interceptor {

        private  final Charset UTF8 = Charset.forName("UTF-8");

        @Override
        public okhttp3.Response intercept(Chain chain) throws IOException {
            Request request = chain.request();
            TRACE.d("token expired 11");

            // try the request
            okhttp3.Response originalResponse = chain.proceed(request);

            /**通过如下的办法曲线取到请求完成的数据
             *
             * 原本想通过  originalResponse.body().string()
             * 去取到请求完成的数据,但是一直报错,不知道是okhttp的bug还是操作不当
             *
             * 然后去看了okhttp的源码,找到了这个曲线方法,取到请求完成的数据后,根据特定的判断条件去判断token过期
             */
            ResponseBody responseBody = originalResponse.body();
            BufferedSource source = responseBody.source();
            source.request(Long.MAX_VALUE); // Buffer the entire body.
            Buffer buffer = source.buffer();
            Charset charset = UTF8;
            MediaType contentType = responseBody.contentType();
            if (contentType != null) {
                charset = contentType.charset(UTF8);
            }

            String bodyString = buffer.clone().readString(charset);
            TRACE.d("token expired = "+bodyString);

            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(bodyString);
                String code = jsonObject.getString("code");
                if(code.equals("401")){
                    isTokenExpired = true;
                }else {
                    isTokenExpired = false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /***************************************/

            if (isTokenExpired){//根据和服务端的约定判断token过期

//                //取出本地的refreshToken
//                String refreshToken = "sssgr122222222";
//
//                // 通过一个特定的接口获取新的token，此处要用到同步的retrofit请求
//                ApiService service = getInstance().mRetrofit.create(ApiService.class);
//                JSONObject paramMap = new JSONObject();
//                RequestBody body = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), paramMap.toString());
//                Call<String> call = service.refreshToken(getHeaders(RetrofitHttp.getContext()),"/api/refreshToken",body);
//
//                //要用retrofit的同步方式
//                String newToken = call.execute().body();
//                TRACE.d("new token = "+newToken);
//
//                // create a new request and modify it accordingly using the new token
//                Request newRequest = request.newBuilder().header("token", newToken)
//                        .build();
//
//                // retry the request
//
//                originalResponse.body().close();
//                return chain.proceed(newRequest);
            }

            // otherwise just pass the original response on
            return originalResponse;
        }
    }

}
