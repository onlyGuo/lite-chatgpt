package com.guoshengkai.litechatgpt.core;

public interface CommAttr {

    String CACHE_SYSTEM_MESSAGE_COUNT = "CACHE_SYSTEM_MESSAGE_COUNT_";

    interface REDIS_CACHE {
        String MENUS = "SYS:MENUS";
    }

    interface CHANNEL {
        String GROUP = "GROUP";
        String CHAT = "CHAT";
        String POST = "POST";
    }

    interface MESSAGE_TYPE {
        int SYSTEM = 0;
        int POST = 1;
        int user = 2;
    }

}
