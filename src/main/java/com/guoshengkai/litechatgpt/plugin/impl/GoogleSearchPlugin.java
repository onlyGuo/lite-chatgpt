package com.guoshengkai.litechatgpt.plugin.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.guoshengkai.litechatgpt.core.util.http.HTTP;
import com.guoshengkai.litechatgpt.core.util.http.HttpHeader;
import com.guoshengkai.litechatgpt.plugin.sdk.GPTPluginMethodParameter;
import com.guoshengkai.litechatgpt.plugin.sdk.Plugin;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class GoogleSearchPlugin implements Plugin {

    @Override
    public String getPluginName() {
        return "Google Search";
    }

    @Override
    public String getPluginDescription() {
        return "Use Google to search for the latest content on the internet.";
    }

    @Override
    public String getPluginMethodName() {
        return "searchFromGoogle";
    }

    @Override
    public String getMethodDescription() {
        return "Use Google to search for the latest content on the internet.";
    }

    @Override
    public String getPluginIcon() {
        return "/icons/plugins/google.svg";
    }

    @Override
    public List<GPTPluginMethodParameter> getPluginMethodParameters() {
        return List.of(
                new GPTPluginMethodParameter(
                        "query",
                        "string",
                        "The search query, the maximum length is 20 characters",
                        null,
                        true
                )
        );
    }

    @Override
    public String execute(Map<String, Object> params, String baseUrl, String accessToken) {
        System.out.println("bing search ...");
        String query = URLEncoder.encode(params.get("query").toString(), StandardCharsets.UTF_8);
        JSONObject jsonObject = HTTP.get("https://api.bing.microsoft.com/v7.0/search?q=" + query + "&mkt=zh-CN",
                HttpHeader.of("Ocp-Apim-Subscription-Key", System.getenv("AZURE_BING_SEARCH_API_KEY")),
                Map.of());
        StringBuilder result = new StringBuilder();
        JSONArray pages = jsonObject.getJSONObject("webPages").getJSONArray("value");

        for(int i = 0; i < pages.size(); i++){
            if(i <= 6){
                result.append(pages.getJSONObject(i).getString("snippet")).append("\n");
            }
        }
        return jsonObject.toJSONString();
    }
}
