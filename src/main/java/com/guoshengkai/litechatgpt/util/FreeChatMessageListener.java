package com.guoshengkai.litechatgpt.util;

import java.io.IOException;

public interface FreeChatMessageListener {
    void handler(FreeMessage message) throws IOException;
}
