package com.guoshengkai.litechatgpt.core.util.http;

import java.util.HashMap;
import java.util.Map;

public class HttpHeader {

    Map<String, Object> headers = new HashMap<>();

    private HttpHeader(){

    }

    public static HttpHeader of(String key, Object value){
        return new HttpHeader().append(key, value);
    }

    public HttpHeader append(String key, Object value){
        headers.put(key, value);
        return this;
    }

    public Map<String, Object> getHeaders(){
        return headers;
    }

}
