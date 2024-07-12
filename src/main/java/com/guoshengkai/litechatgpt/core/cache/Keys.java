package com.guoshengkai.litechatgpt.core.cache;

/**
 * Copyright (c) 2021 HEBEI CLOUD IOT FACTORY BIGDATA CO.,LTD.
 * Legal liability shall be investigated for unauthorized use
 *
 * @Author: Guo Shengkai
 * @Date: Create in 2021/04/08 9:10
 */
public class Keys {

    private Keys() {}

    private String key;

    public static Keys keys(String key, Object... keys){
        Keys keysObj = new Keys();
        keysObj.key = key;
        if (key.endsWith(":")){
            key = key.substring(0, key.length() - 1);
        }

        if (keys.length > 0){
            StringBuffer buffer = new StringBuffer();
            for (Object k: keys){
                buffer.append(":").append(k);
            }
            keysObj.key += buffer.toString();
        }

        return keysObj;
    }

    public Keys of(String key, Object... keys){
        if (key.endsWith(":")){
            key = key.substring(0, key.length() - 1);
        }

        if (keys.length > 0){
            StringBuilder buffer = new StringBuilder();
            for (Object k: keys){
                buffer.append(":").append(k);
            }
            this.key += ":" + key + buffer;
        } else {
            this.key += ":" + key;
        }

        return this;
    }

    @Override
    public String toString() {
        return key;
    }
}