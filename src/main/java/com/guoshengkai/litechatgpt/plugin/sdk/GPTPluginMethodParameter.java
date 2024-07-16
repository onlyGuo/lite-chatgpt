package com.guoshengkai.litechatgpt.plugin.sdk;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class GPTPluginMethodParameter {
    /**
     * 参数名称
     */
    private String name;
    /**
     * 参数类型
     */
    private String type;
    /**
     * 参数描述
     */
    private String description;
    /**
     * 参数枚举值
     */
    private List<String> enums;

    public GPTPluginMethodParameter() {
    }

    /**
     * 构造方法
     * @param name
     *      参数名称
     * @param type
     *      参数类型
     * @param description
     *      参数描述
     * @param enums
     *      参数枚举值
     */
    public GPTPluginMethodParameter(String name, String type, String description, List<String> enums) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.enums = enums;
    }

}
