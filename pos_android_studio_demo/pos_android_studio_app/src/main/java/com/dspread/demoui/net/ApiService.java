package com.dspread.demoui.net;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ApiService {
    @GET
    Call<ResponseBody> get(@HeaderMap Map<String, String> headers, @Url String url, @QueryMap Map<String, Object> param);

    @GET
    Call<ResponseBody> get(@Url String url, @Body RequestBody requestBody);

    @GET
    Call<ResponseBody> getStr(@HeaderMap Map<String, String> headers,@Url String url, @Body RequestBody requestBody);

//    @GET
//    Call<ResponseBody> getStr(@Url String url, @Query("page")String page);

    @POST
    Call<ResponseBody> postStr(@HeaderMap Map<String, String> headers,@Url String url, @Body RequestBody requestBody);

    @FormUrlEncoded
    @POST
    Call<ResponseBody> post(@Url String url, @FieldMap Map<String, Object> param);

    @Multipart
    @POST
    Call<String> postUpload(@Url String url, @HeaderMap Map<String, Object> headers, @QueryMap Map<String, Object> paramsField/*, @Part("filedes") String des*/, @PartMap Map<String, RequestBody> params);

    @Multipart
    @GET
    Call<String> getUpload(@Url String url, @HeaderMap Map<String, Object> headers, @FieldMap Map<String, Object> paramsField,/*@Part("filedes") String des,*/ @PartMap Map<String, RequestBody> params);

    @Multipart
    @POST
    Call<ResponseBody> upload(@Url String url, @Part("json") RequestBody body, @Part MultipartBody.Part file);

    @Multipart
    @POST
    Call<ResponseBody> upload(@Url String url, @Part MultipartBody.Part file);

    @POST
    Call<ResponseBody> postBody(@HeaderMap Map<String, String> headers, @Url String url, @Body RequestBody requestBody);

    @PUT
    Call<ResponseBody> put(@Url String url, @Body RequestBody requestBody);

    @PUT
    Call<ResponseBody> put(@Url String url, @QueryMap Map<String, Object> param);

    @HTTP(method = "DELETE",hasBody = true)
    Call<ResponseBody> delete(@Url String url, @Body RequestBody requestBody);

    @DELETE
    Call<ResponseBody> delete(@Url String url, @QueryMap Map<String, Object> param);

    @Streaming//防止大文件下载造成oom
    @GET
    Call<ResponseBody> download(@Url String url);

    @POST
    Call<String> refreshToken(@HeaderMap Map<String, String> headers, @Url String url, @Body RequestBody requestBody);

}
