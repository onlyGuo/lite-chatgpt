package com.guoshengkai.litechatgpt.core.cache;

public interface LockRunner<T> {

    T exec();
}
