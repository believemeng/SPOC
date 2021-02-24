package com.dspread.demoui.net.retrofitUtil;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;

/**
 * Created by lyl on 2016/11/6.
 */

public class RequestUtil {
    private Map<String, Object> mHeaderMap;
    private Map<String, Object> mParamsMap;
    private JSONObject objectParams;
    private JSONArray arrayParams;
    private Map<String, File> mUploadFiles;
    private File file;
    private String mApiUlr;
    private RequestMethod mRequestMethod = RequestMethod.GET;

    private static MediaType mMediaType = MediaType.parse("application/octet-stream");

    public RequestUtil(String apiUlr, RequestMethod method) {
        this.mApiUlr = apiUlr;
        this.mRequestMethod = method;
    }


    public RequestUtil putHeader(String key, Object value) {
        if (mHeaderMap == null) {
            mHeaderMap = new HashMap<>();
        }
        mHeaderMap.put(key, value);
        return this;
    }

    public void putHeaderMap(Map<String, Object> headerMap) {
        if (mHeaderMap != null) {
            mHeaderMap.putAll(headerMap);
        } else {
            mHeaderMap = headerMap;
        }
    }

    public RequestUtil putParams(String key, Object value) {
        if (mParamsMap == null) {
            mParamsMap = new HashMap<>();
        }
        mParamsMap.put(key, value);
        return this;
    }

    public void putParamsObj(JSONObject obj){
        if(objectParams == null){
            objectParams = new JSONObject();
        }
        objectParams = obj;
    }

    public void putParamsArray(JSONArray arrayParams){
        if(this.arrayParams == null){
            this.arrayParams = new JSONArray();
        }
        this.arrayParams = arrayParams;
    }

    public JSONArray getArrayParams(){
        return this.arrayParams;
    }

    public void putParamsMap(Map<String, Object> paramMap) {
        if (mParamsMap != null) {
            mParamsMap.putAll(paramMap);
        } else {
            mParamsMap = paramMap;
        }
    }

    public RequestUtil putMediaType(MediaType mediaType) {
        mMediaType = mediaType;
        return this;
    }

    public static MediaType getMediaType() {
        return mMediaType;
    }

    public RequestUtil putUploadFile(String key, File uploadFile) {
        if (mUploadFiles == null) {
            mUploadFiles = new HashMap<>();
        }
        mUploadFiles.put(key, uploadFile);
        return this;
    }

    public RequestUtil putFile(File uploadFile){
        this.file = uploadFile;
        return this;
    }

    public File getFile(){
        return this.file;
    }

    public Map<String, File> getUploadFiles() {
        return mUploadFiles;
    }

    public Map<String, Object> getHeaderMap() {
        return mHeaderMap;
    }

    public Map<String, Object> getParamsMap() {
        return mParamsMap;
    }

    public JSONObject getObjParams(){
        return objectParams;

    }
    public String getApiUlr() {
        return mApiUlr;
    }

    public RequestMethod getRequestMethod() {
        return mRequestMethod;
    }
}
