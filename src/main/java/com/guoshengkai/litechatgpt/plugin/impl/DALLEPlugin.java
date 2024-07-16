package com.guoshengkai.litechatgpt.plugin.impl;

import com.alibaba.fastjson.JSONObject;
import com.guoshengkai.litechatgpt.core.util.http.HTTP;
import com.guoshengkai.litechatgpt.core.util.http.HttpHeader;
import com.guoshengkai.litechatgpt.plugin.sdk.GPTPluginMethodParameter;
import com.guoshengkai.litechatgpt.plugin.sdk.Plugin;

import java.util.List;
import java.util.Map;

public class DALLEPlugin implements Plugin {


    @Override
    public String getPluginName() {
        return "DALL·E";
    }

    @Override
    public String getPluginDescription() {
        return "Generate images from prompt descriptions.";
    }

    @Override
    public String getPluginMethodName() {
        return "generateImage";
    }

    @Override
    public String getMethodDescription() {
        return "Draw a picture and return the URL and description of the picture. " +
                "You need to use ![img](url) to display the picture.";
    }

    @Override
    public String getPluginIcon() {
        return "/icons/plugins/dalle.svg";
    }

    private final List<String> sizeEnums = List.of("1024x1024", "1792x1024", "1024x1792");

    @Override
    public List<GPTPluginMethodParameter> getPluginMethodParameters() {
        return List.of(
                new GPTPluginMethodParameter(
                        "prompt",
                        "string",
                        "A text description of the desired image(s). The maximum length is 4000 characters",
                        null,
                        true
                ),
                new GPTPluginMethodParameter(
                        "size",
                        "string",
                        "The size of the image, default value: 1024x1024",
                        sizeEnums,
                        true
                )
        );
    }

    @Override
    public String execute(Map<String, Object> params, String baseUrl, String accessToken) {
        baseUrl = baseUrl.endsWith("/") ? baseUrl : baseUrl + "/";
        // 默认size=1024
        if(params.get("size") != null && !sizeEnums.contains(params.get("size").toString())){
            params.put("size", "1024x1024");
        }

        // 请求OpenAI接口，生成图片
        JSONObject post = HTTP.post(baseUrl + "v1/images/generations",
                HttpHeader.of("Authorization", "Bearer " + accessToken)
                        .append("Content-Type", "application/json; charset=UTF-8"),
                Map.of("model", "dall-e-3",
                        "prompt", params.get("prompt"),
                        "n", 1,
                        "size", params.get("size") == null ? "1024x1024" : params.get("size"),
                        "quality", "standard",
                        "response_format", "url",
                        "style", "vivid")
        );
        return post.toJSONString();
    }
}
