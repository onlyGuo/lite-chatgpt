package com.guoshengkai.litechatgpt.core.cache;

import java.io.IOException;

/**
 * Copyright (c) 2021 HEBEI CLOUD IOT FACTORY BIGDATA CO.,LTD.
 * Legal liability shall be investigated for unauthorized use
 *
 * @Author: Li hailong
 * @Date: Create in 2021/8/6 17:18
 */
public interface RunnableIO {

    void run() throws IOException;
}
