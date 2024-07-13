package com.guoshengkai.litechatgpt.entity.vo;

import com.guoshengkai.litechatgpt.entity.Message;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class MessageRequest {

    private List<Message> messages;

    private String model;

    private int chatId;

}
