package com.guoshengkai.litechatgpt.entity.vo;

import com.guoshengkai.litechatgpt.plugin.sdk.Plugin;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PluginEntity {

    private String pluginName;

    private String pluginDescription;

    private String pluginIcon;


    public static PluginEntity formPlugin(Plugin plugin){
        PluginEntity pluginEntity = new PluginEntity();
        pluginEntity.setPluginName(plugin.getPluginName());
        pluginEntity.setPluginDescription(plugin.getPluginDescription());
        pluginEntity.setPluginIcon(plugin.getPluginIcon());
        return pluginEntity;
    }

}
