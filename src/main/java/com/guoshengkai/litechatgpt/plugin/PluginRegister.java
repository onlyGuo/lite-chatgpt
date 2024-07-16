package com.guoshengkai.litechatgpt.plugin;

import com.guoshengkai.litechatgpt.plugin.sdk.Plugin;
import org.reflections.Reflections;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class PluginRegister {

    private static final LinkedHashMap<String, Plugin> PLUGINS = new LinkedHashMap<>();

    private static final LinkedHashMap<String, Plugin> PLUGINS_FUN_MAPPER = new LinkedHashMap<>();

    static {
        // 扫描impl包下的所有类
        String packageName = PluginRegister.class.getPackageName() + ".impl"; // 替换为你的包名
        Reflections reflections = new Reflections(packageName);
        Set<Class<? extends Plugin>> classes = reflections.getSubTypesOf(Plugin.class);
        for (Class<? extends Plugin> clazz : classes) {
            try {
                registerPlugin(clazz.getDeclaredConstructor().newInstance());
            } catch (InstantiationException | InvocationTargetException | IllegalAccessException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static void registerPlugin(Plugin plugin){
        PLUGINS.put(plugin.getPluginName(), plugin);
        PLUGINS_FUN_MAPPER.put(plugin.getPluginMethodName(), plugin);
        //TODO
    }

    public static List<Plugin> getPlugins(){
        return PLUGINS.values().stream().toList();
    }

    public static Plugin getPlugin(String pluginName){
        return PLUGINS.get(pluginName);
    }

    public static Plugin getPluginByMethodName(String methodName){
        return PLUGINS_FUN_MAPPER.get(methodName);
    }
}
