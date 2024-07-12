package com.guoshengkai.litechatgpt.core.cache;

public interface CacheCallBack {

    void call(Keys key, Object value);

}
