package com.guoshengkai.litechatgpt.core.cache;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;


/**
 * Copyright (c) 2021 HEBEI CLOUD IOT FACTORY BIGDATA CO.,LTD.
 * Legal liability shall be investigated for unauthorized use
 *
 * 缓存相关配置
 * @Author: Guo Shengkai
 * @Date: Create in 2021/04/08 9:09
 */
@Component
public class CacheConfiguration {

    protected Logger logger = LoggerFactory.getLogger(CacheConfiguration.class);

    @Resource
    private RedisTemplate<Object, Object> redisTemplate;

    @PostConstruct
    public void init(){
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        CacheUtil.setTemplate(redisTemplate);
        logger.info("Init Redis connection success!");
    }

}
