package com.guoshengkai.litechatgpt.plugin.impl;

import com.guoshengkai.litechatgpt.core.util.http.HTTP;
import com.guoshengkai.litechatgpt.plugin.sdk.GPTPluginMethodParameter;
import com.guoshengkai.litechatgpt.plugin.sdk.Plugin;

import java.util.List;
import java.util.Map;

public class BrowsePlugin implements Plugin {
    @Override
    public String getPluginName() {
        return "Browse Web";
    }

    @Override
    public String getPluginDescription() {
        return "Browse the content of a website.";
    }

    @Override
    public String getPluginMethodName() {
        return "browse_web";
    }

    @Override
    public String getMethodDescription() {
        return "Browse the content of a website.";
    }

    @Override
    public String getPluginIcon() {
        return "/icons/plugins/browser.svg";
    }

    @Override
    public List<GPTPluginMethodParameter> getPluginMethodParameters() {
        return List.of(
                new GPTPluginMethodParameter(
                        "url",
                        "string",
                        "The URL of the website to browse.",
                        null,
                        true
                )
        );
    }

    @Override
    public String execute(Map<String, Object> params, String baseUrl, String accessToken) {
        String string = HTTP.getText(params.get("url").toString(), Map.of());
        // 提取html中的文字
        string = string.replaceAll("<[^>]*>", "");
        string = string.replaceAll("\\s*", "");
        return string;
    }
}
