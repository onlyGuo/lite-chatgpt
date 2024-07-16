package com.guoshengkai.litechatgpt.plugin.sdk;

import java.util.List;
import java.util.Map;

public interface Plugin {

    String getPluginName();

    String getPluginDescription();

    String getMethodDescription();

    String getPluginIcon();

    List<GPTPluginMethodParameter> getPluginMethodParameters();

    String execute(Map<String, Object> params, String baseUrl, String accessToken);


}
