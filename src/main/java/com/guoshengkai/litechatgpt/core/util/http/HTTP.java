package com.guoshengkai.litechatgpt.core.util.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Http 快捷操作类
 */
@Slf4j
public class HTTP {

    /**
     * 发送Get请求
     * @param url
     *      请求地址
     * @param data
     *      请求信息
     * @return
     *      响应信息
     */
    public static JSONObject get(String url, Object data){
        return JSON.parseObject(requestCommon(url, data, "GET"));
    }

    /**
     * 发送Get请求
     * @param url
     *      请求地址
     * @return
     *      响应信息
     * @param data
     *      请求信息
     * @return
     *      响应信息
     */
    public static JSONObject get(String url, HttpHeader header, Object data){
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(data));
        jsonObject.put("header", header.getHeaders());
        return get(url, jsonObject);
    }

    /**
     * 发送POST请求
     * @param url
     *      请求地址
     * @param data
     *      请求信息
     * @return
     *      响应信息
     */
    public static JSONObject post(String url, Object data){
        return JSON.parseObject(requestCommon(url, data, "POST"));
    }

    /**
     * 发送POST请求
     * @param url
     *      请求地址
     * @param header
     *      请求头
     * @param data
     *      请求信息
     * @return
     *      响应信息
     */
    public static JSONObject post(String url, HttpHeader header, Object data){
        return post(url, Map.of("header", header.getHeaders(), "data", data));
    }

    /**
     * 发送DELETE请求
     * @param url
     *      请求地址
     * @param data
     *      请求信息
     * @return
     *      响应信息
     */
    public static JSONObject delete(String url, Object data){
        return JSON.parseObject(requestCommon(url, data, "DELETE"));
    }

    /**
     * 发送PUT请求
     * @param url
     *      请求地址
     * @param data
     *      请求信息
     * @return
     *      响应信息
     */
    public static JSONObject put(String url, Object data){
        return JSON.parseObject(requestCommon(url, data, "PUT"));
    }


    @SneakyThrows
    private static String requestCommon(String url, Object data, String method){
        log.info("HTTP Request [{}]", url);
        Map header = new HashMap();
        String dataStr = null;
        if(null != data){
            Map parse = JSON.parseObject(JSON.toJSONString(data), Map.class);
            if(parse.get("header") != null){
                header = JSON.parseObject(JSON.toJSONString(parse.get("header")), Map.class);
            }
            if (parse.get("data") != null){
                dataStr = JSON.toJSONString(parse.get("data"));
                log.info("HTTP Request Data: {}", dataStr);
            }
        }
        CloseableHttpClient client = HttpClients.createDefault();
        BaseRequest baseRequest = new BaseRequest(url, method);
        for (Object key: header.keySet()){
            baseRequest.setHeader(key.toString(), header.get(key).toString());
        }
        if (null != dataStr){
            baseRequest.setEntity(new StringEntity(dataStr, StandardCharsets.UTF_8));
        }
        try (CloseableHttpResponse execute = client.execute(baseRequest)) {
            return EntityUtils.toString(execute.getEntity(), StandardCharsets.UTF_8);
        }
    }

    public static String getText(String url, Object data) {
        return requestCommon(url, data, "GET");
    }

    static class BaseRequest extends HttpEntityEnclosingRequestBase{
        private final String METHOD_NAME;

        public BaseRequest(String uri, String methodName) {
            this.setURI(URI.create(uri));
            METHOD_NAME = methodName;
        }

        public String getMethod() {
            return METHOD_NAME;
        }
    }
}
