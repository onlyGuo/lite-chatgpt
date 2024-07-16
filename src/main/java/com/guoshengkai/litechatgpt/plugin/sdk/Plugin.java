package com.guoshengkai.litechatgpt.plugin.sdk;

import java.util.*;

public interface Plugin {

    String getPluginName();

    String getPluginDescription();

    String getPluginMethodName();

    String getMethodDescription();

    String getPluginIcon();

    List<GPTPluginMethodParameter> getPluginMethodParameters();

    String execute(Map<String, Object> params, String baseUrl, String accessToken);

    private Map<String, Object> getPluginMethodParametersMap(){
        List<GPTPluginMethodParameter> pluginMethodParameters = getPluginMethodParameters();
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        List<String> required = new ArrayList<>();
        for (GPTPluginMethodParameter pluginMethodParameter : pluginMethodParameters) {
            LinkedHashMap<String, Object> paramsMap = new LinkedHashMap<>(Map.of(
                    "type", pluginMethodParameter.getType(),
                    "description", pluginMethodParameter.getDescription()
            ));
            if (pluginMethodParameter.getEnums() != null && !pluginMethodParameter.getEnums().isEmpty()) {
                paramsMap.put("enums", pluginMethodParameter.getEnums());
            }
            map.put(pluginMethodParameter.getName(), paramsMap);

            // 是否必填
            if (pluginMethodParameter.isRequired()) {
                required.add(pluginMethodParameter.getName());
            }
        }
        return Map.of(
                "type", "object",
                "properties", map,
                "required", required
        );
    }

    default Map<String, Object> toMap(){
        return Map.of(
                "type", "function",
                "function", Map.of(
                    "name", getPluginMethodName(),
                    "description", getMethodDescription(),
                    "parameters", getPluginMethodParametersMap()
                )
        );
    }

}
