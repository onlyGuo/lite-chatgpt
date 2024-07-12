package com.guoshengkai.litechatgpt.core.cache;

import com.alibaba.fastjson.JSON;
import com.guoshengkai.litechatgpt.exception.ServiceInvokeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Copyright (c) 2021 HEBEI CLOUD IOT FACTORY BIGDATA CO.,LTD.
 * Legal liability shall be investigated for unauthorized use
 *
 * 缓存工具类
 * @Author: Guo Shengkai
 * @Date: Create in 2021/04/08 9:10
 */
public class CacheUtil {

    private static RedisTemplate<Object, Object> REDIS_TEMPLATE;

    protected static final Logger LOGGER = LoggerFactory.getLogger(CacheUtil.class);

    /**
     * 设置Redis数据模板
     * @param redisTemplate
     *      数据模板对象
     */
    protected static void setTemplate(RedisTemplate<Object, Object> redisTemplate){
        CacheUtil.REDIS_TEMPLATE = redisTemplate;
    }

    /**
     * 获取一个对象
     * @param key
     *      Key
     * @param clazz
     *      对象类型
     * @return
     */
    @SuppressWarnings("unchecked")
    public static<T> T getObject(String key, Class<T> clazz){
        Object o = REDIS_TEMPLATE.opsForValue().get(key);
        if (null == o){
            return null;
        }
        if (clazz.toString().equals(String.class.toString())){
            return (T)o;
        }
        return JSON.parseObject(o.toString(), clazz);
    }

    /**
     * 获取一个对象
     * @param key
     *      Key
     * @param clazz
     *      对象类型
     * @return
     */
    public static<T> T getObject(Keys key, Class<T> clazz){
        return getObject(key.toString(), clazz);
    }

    /**
     * 存入一个对象
     * @param key
     *      对象Key
     * @param value
     *      对象Value
     */
    public static void pushObject(String key, Object value){
        pushObject(Keys.keys(key), value, 0);
    }

    /**
     * 存入一个对象
     * @param key
     *      对象Key
     * @param value
     *      对象Value
     */
    public static void pushObject(Keys key, Object value){
        pushObject(key.toString(), value);
    }

    /**
     * 存入一个有过期时间的对象
     * @param key
     *      对象Key
     * @param value
     *      对象Value
     * @param expireTime
     *      有效时间(秒), 小于或等于0时, 表示无限时
     */
    @SuppressWarnings("unchecked")
    public static void pushObject(Keys key, Object value, long expireTime){
        String jsonValue = value instanceof String ? value.toString() : JSON.toJSONString(value);
        if (0 < expireTime){
            REDIS_TEMPLATE.opsForValue().set(key.toString(), jsonValue, expireTime, TimeUnit.SECONDS);
        }else{
            REDIS_TEMPLATE.opsForValue().set(key.toString(), JSON.toJSONString(value));
        }
    }

    /**
     * 删除一个对象
     * @param key
     *      对象的Key
     */
    public static void removeObject(Keys key){
        REDIS_TEMPLATE.delete(key.toString());
    }

    /**
     * 设置一个对象的过期时间
     * @param keys
     *      对象Key
     * @param expireTime
     *      有效时间(秒)
     */
    public static void setExpireTime(Keys keys, long expireTime){
        REDIS_TEMPLATE.expire(keys.toString(), expireTime, TimeUnit.SECONDS);
    }


    /**
     * 在List集合中存入一个对象
     * @param keys
     *      对象Key
     * @param value
     *      对象Value
     */
    @SuppressWarnings({"ConstantConditions"})
    public static long addForList(Keys keys, Object value){
        String jsonValue = value instanceof String ? value.toString() : JSON.toJSONString(value);
        return REDIS_TEMPLATE.opsForList().rightPush(keys.toString(), jsonValue);
    }

    /**
     * 在List集合中存入一个对象
     * @param keys
     *      集合Key
     * @param value
     *      对象Value
     */
    @SuppressWarnings({"ConstantConditions"})
    public static long addForList(Keys keys, Object... value){
        String[] strings = new String[value.length];
        for (int i = 0; i < value.length; i++){
            strings[i] = value[i] instanceof String ? value[i].toString() : JSON.toJSONString(value[i]);
        }
        return REDIS_TEMPLATE.opsForList().rightPushAll(keys.toString(), strings);
    }

    /**
     * 从List集合中取出一个对象
     * @param keys
     *      集合Key
     * @param index
     *      集合中对象索引
     * @param clazz
     *      对象类型
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static<T> T getForList(Keys keys, long index, Class<T> clazz){
        Object value = REDIS_TEMPLATE.opsForList().index(keys.toString(), index);
        if (null == value){
            return null;
        }
        if (clazz.toString().equals(String.class.toString())){
            return (T)value.toString();
        }
        return JSON.parseObject(value.toString(), clazz);
    }

    /**
     * 获取List中首个元素并删除他
     * @param keys
     *      集合Key
     * @param clazz
     *      元素类型
     * @param timeout
     *      若无元素时, 最大等待时间(秒)
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static<T> T getFirstForListAndRemove(Keys keys, Class<T> clazz, int timeout){
        Object value = REDIS_TEMPLATE.opsForList().leftPop(keys.toString(), timeout, TimeUnit.SECONDS);

        if (null == value){
            return null;
        }
        if (clazz.toString().equals(String.class.toString())){
            return (T)value.toString();
        }
        return JSON.parseObject(value.toString(), clazz);
    }

    /**
     * 移除并获取List中的末尾元素
     * @param keys
     *      集合Key
     * @param clazz
     *      元素类型
     * @param timeout
     *      若无元素时, 最大等待时间(秒)
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static<T> T getLastForListAndRemove(Keys keys, Class<T> clazz, int timeout){
        Object value = REDIS_TEMPLATE.opsForList().rightPop(keys.toString(), timeout, TimeUnit.SECONDS);

        if (null == value){
            return null;
        }
        if (clazz.toString().equals(String.class.toString())){
            return (T)value.toString();
        }
        return JSON.parseObject(value.toString(), clazz);
    }

    /**
     * 获取List中的第一个元素
     * @param keys
     *      集合Key
     * @param clazz
     *      元素类型
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static<T> T getFirstForList(Keys keys, Class<T> clazz){
        Object value = REDIS_TEMPLATE.opsForList().index(keys.toString(), 0);

        if (null == value){
            return null;
        }
        if (clazz.toString().equals(String.class.toString())){
            return (T)value.toString();
        }
        return JSON.parseObject(value.toString(), clazz);
    }

    /**
     * 获取List中的最后一个元素
     * @param keys
     *      集合Key
     * @param clazz
     *      元素类型
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static<T> T getLastForList(Keys keys, Class<T> clazz){
        Object value = REDIS_TEMPLATE.opsForList().index(keys.toString(), -1);

        if (null == value){
            return null;
        }
        if (clazz.toString().equals(String.class.toString())){
            return (T)value.toString();
        }
        return JSON.parseObject(value.toString(), clazz);
    }

    /**
     * 获取一个集合的大小
     * @param keys
     *      集合Key
     * @return
     */
    @SuppressWarnings("ConstantConditions")
    public static long getSizeForList(Keys keys){
        return REDIS_TEMPLATE.opsForList().size(keys.toString());
    }


    /**
     * 移除集合中的一个元素
     * @param keys
     *      集合Key
     * @param index
     *      元素索引
     */
    public static void removeForList(Keys keys, long index){
        REDIS_TEMPLATE.opsForList().remove(keys.toString(), 0, Objects.requireNonNull(getForList(keys, index, String.class)));
    }


    /**
     * 在Map中添加一个对象
     * @param keys
     *      集合Key
     * @param index
     *      Map索引
     * @param value
     *      要添加的对象
     * @return
     */
    public static String putForMap(Keys keys, String index, Object value){
        String jsonValue = value instanceof String ? value.toString() : JSON.toJSONString(value);
        REDIS_TEMPLATE.opsForHash().put(keys.toString(), index, jsonValue);
        return index;
    }

    /**
     * 从Map中获取一个对象
     * @param keys
     *      集合Key
     * @param index
     *      集合中对象的索引
     * @param clazz
     *      对象类型
     * @return
     */
    @SuppressWarnings({"unchecked"})
    public static<T> T getForMap(Keys keys, String index, Class<T> clazz){
        Object value = REDIS_TEMPLATE.opsForHash().get(keys.toString(), index);
        if (null == value){
            return null;
        }
        if (clazz.toString().equals(String.class.toString())){
            return (T)value.toString();
        }
        return JSON.parseObject(value.toString(), clazz);
    }

    public static Map<Object, Object> getAllForMap(Keys keys){
        return  REDIS_TEMPLATE.opsForHash().entries(keys.toString());
    }

    /**
     * 从Map中删除一个对象
     * @param keys
     *      集合Key
     * @param index
     *      集合中对象的索引
     */
    public static void removeForMap(Keys keys, String index){
        REDIS_TEMPLATE.opsForHash().delete(keys.toString(), getForMap(keys, index, String.class));
    }


    /**
     * 获得分布式锁
     * @param key
     *      锁Key
     * @param expire
     *      锁过期时间
     * @return
     */
    public static boolean lock(String key, int expire){
        String lockName = "LOCK:" + key;
        long value = System.currentTimeMillis() + expire;
        LOGGER.debug("分布式锁开启 ---> " + lockName + " ---> " + expire);
        try {
            Boolean setIfAbsent = REDIS_TEMPLATE.opsForValue().setIfAbsent(lockName, String.valueOf(value), expire, TimeUnit.MILLISECONDS);
            if(null != setIfAbsent && setIfAbsent) {
                return true;
            }

            // 未成功占位, 判断是否有超时锁
            Long oldExpireTime = getObject(lockName, Long.class);
            if (null == oldExpireTime){
                oldExpireTime = 0L;
            }
            if(oldExpireTime < System.currentTimeMillis()) {
                //锁超时, 释放并新锁
                long newExpireTime = System.currentTimeMillis() + expire;
                Object andSet = REDIS_TEMPLATE.opsForValue().getAndSet(lockName, String.valueOf(newExpireTime));
                long currentExpireTime = Long.parseLong(null == andSet ? "0" : andSet.toString());
                if(currentExpireTime == oldExpireTime) {
                    return true;
                }
            }
        }catch (Exception e){
            LOGGER.error("分布式锁上锁失败:" + e.getMessage(), e);
        }
        return false;
    }

    /**
     * 释放分布式锁
     * @param key
     */
    public static void unLock(String key) {
        String lockName = "LOCK:" + key;
        LOGGER.debug("分布式锁结束 ---> " + lockName);
        Long value = getObject(lockName, Long.class);
        if (value != null){
            long oldExpireTime = value;
            if(oldExpireTime > System.currentTimeMillis()) {
                removeObject(Keys.keys(lockName));
            }
        }
    }

    /**
     * 在分布式锁内执行
     * @param key
     *      锁名称
     * @param expire
     *      锁超时时间
     * @param runnable
     *      执行的代码
     */
    public static void lock(String key, int expire, Runnable runnable){
        // 上锁
        if (!CacheUtil.lock(key, expire)){
            throw new ServiceInvokeException("服务器忙, 请稍后再试");
        }
        try {
            // 执行代码
            runnable.run();
        }finally {
            // 释放锁
            CacheUtil.unLock(key);
        }
    }


    public static void lockIO(String key, int expire, RunnableIO runnable){
        // 上锁
        if (!CacheUtil.lock(key, expire)){
            throw new ServiceInvokeException("服务器忙, 请稍后再试");
        }
        try {
            // 执行代码
            runnable.run();
        }catch (IOException e){
            throw new RuntimeException(e);
        }finally {
            // 释放锁
            CacheUtil.unLock(key);
        }
    }





    /************************************************************201909260824迁移*******************************************************************/

    public static final String LOCK_PREFIX = "redis_lock";
    public static final int LOCK_EXPIRE = 4000;

    /**
     *  最终加强分布式锁
     *
     * @param key key值
     * @return 是否获取到
     */
    public static boolean lock(String key){
        String lock = LOCK_PREFIX + key;
        // 利用lambda表达式
        Boolean boo = (Boolean) REDIS_TEMPLATE.execute((RedisCallback<?>) connection -> {

            long expireAt = System.currentTimeMillis() + LOCK_EXPIRE + 1;
            Boolean bool = connection.setNX(lock.getBytes(), String.valueOf(expireAt).getBytes());
            if (null != bool && bool) {
                return true;
            } else {
                byte[] value = connection.get(lock.getBytes());

                if (Objects.nonNull(value) && value.length > 0) {

                    long expireTime = Long.parseLong(new String(value));

                    if (expireTime < System.currentTimeMillis()) {
                        // 如果锁已经过期
                        byte[] oldValue = connection.getSet(lock.getBytes(), String.valueOf(System.currentTimeMillis() + LOCK_EXPIRE + 1).getBytes());
                        // 防止死锁
                        return null != oldValue && Long.parseLong(new String(oldValue)) < System.currentTimeMillis();
                    }
                }
            }
            return false;
        });
        return null!=boo && boo;
    }

    /**
     * 获得List
     * @param keys
     *      key
     * @param clazz
     *      类型
     * @param <T>
     * @return
     */
    public static <T> List<T> getList(Keys keys, Class<T> clazz) {
        Object o = REDIS_TEMPLATE.opsForValue().get(keys.toString());
        if (null == o){
            return null;
        }
        return JSON.parseArray(o.toString(), clazz);
    }

    /**
     * 本地缓存ID
     */
    private static Map<String, Map<String, Long>> ID_CACHE = new HashMap<>();

    /**
     * 获取下一个ID
     * @param keys
     *      不同的KEY之间ID不冲突
     * @return
     */
    public static synchronized long nextId(Keys keys){
        // 缓存步长值100
        int size = 10;

        Map<String, Long> longs = ID_CACHE.get(keys.toString());
        if (null == longs){
            longs = new HashMap<>();
            longs.put("last", 0L);
            longs.put("next", 0L);
            ID_CACHE.put(keys.toString(), longs);
        }
        long lastId = longs.get("last");
        long nextId = longs.get("next");
        if (lastId <= nextId){
            Long object = getObject(keys, Long.class);
            if (object == null){
                lastId += size;
                nextId = 1;
            }else{
                nextId = object;
                lastId = object + size;
            }
            longs.put("last", lastId);
            pushObject(keys, lastId);
        }
        long res = nextId ++;
        longs.put("next", nextId);
        return res;
    }

    public static void listenKey(Keys key, CacheCallBack call) {
        // 监听Key过期
        REDIS_TEMPLATE.execute((RedisCallback<?>) connection -> {
            connection.pSubscribe((message, pattern) -> {
                String keyStr = new String(message.getChannel());
                String value = new String(message.getBody());
                call.call(Keys.keys(keyStr), value);
            }, key.toString().getBytes());
            return null;
        });
    }

    public static long getExpireTime(Keys key) {
        // 监听Key过期
        Long expire = REDIS_TEMPLATE.getExpire(key.toString(), TimeUnit.SECONDS);
        if (null == expire){
            return 0;
        }
        return expire;
    }
}
