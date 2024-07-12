package com.guoshengkai.litechatgpt.core.annotation.po;


import com.guoshengkai.wechat.chatgpt.core.enums.BigFieldType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BigField {
    BigFieldType value() default BigFieldType.CLOB;
}
