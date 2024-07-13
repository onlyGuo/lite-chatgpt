package com.guoshengkai.litechatgpt.core.util.cacheValue;

import com.guoshengkai.litechatgpt.core.cache.Keys;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CacheValueUtil {

    private static final Map<String, CacheValue<?>> cacheValueMap = new HashMap<>();


    public  static<T> T getValue(ValueCallback<T> callback, Keys keys, TimeUnit timeUnit, long timeOut){
        CacheValue<?> cacheValue = cacheValueMap.get(keys.toString());
        if (cacheValue == null || System.currentTimeMillis() - cacheValue.getTime() > timeUnit.toMillis(timeOut)){
            T value = callback.callback();
            cacheValueMap.put(keys.toString(), new CacheValue<>(value, System.currentTimeMillis()));
            return value;
        }
        return (T) cacheValue.getValue();
    }

    private static class CacheValue<T>{
        private T value;
        private long time;

        public CacheValue(T value, long time){
            this.value = value;
            this.time = time;
        }

        public T getValue(){
            return value;
        }

        public long getTime(){
            return time;
        }
    }
}
