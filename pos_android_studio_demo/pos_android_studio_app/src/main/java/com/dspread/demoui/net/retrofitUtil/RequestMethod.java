package com.dspread.demoui.net.retrofitUtil;

public enum RequestMethod {
    GET("GET"),

    POST("POST"),

    PUT("PUT"),

    DELETE("DELETE");
    private final String value;

    RequestMethod(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
