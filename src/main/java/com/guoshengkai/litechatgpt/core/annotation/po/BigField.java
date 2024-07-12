package com.guoshengkai.litechatgpt.core.annotation.po;



import com.guoshengkai.litechatgpt.core.enums.BigFieldType;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface BigField {
    BigFieldType value() default BigFieldType.CLOB;
}
