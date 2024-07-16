package com.guoshengkai.litechatgpt.controller;

import com.guoshengkai.litechatgpt.conf.NoLogin;
import com.guoshengkai.litechatgpt.entity.vo.PluginEntity;
import com.guoshengkai.litechatgpt.plugin.PluginRegister;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/plugin")
public class PluginController {

    /**
     * 获取所有插件
     * @return
     */
    @NoLogin
    @GetMapping
    public List<PluginEntity> listPlugins() {
        return PluginRegister.getPlugins().stream().map(PluginEntity::formPlugin).toList();
    }

}
