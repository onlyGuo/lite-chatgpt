package com.guoshengkai.litechatgpt.entity.vo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ValidationCodeSendRequest {
    private String email;
    private String mobile;
    private String code;
    private String type;
}
