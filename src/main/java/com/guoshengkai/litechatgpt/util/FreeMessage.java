package com.guoshengkai.litechatgpt.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FreeMessage {
    private String content;
    private boolean over;
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
    public static FreeMessage as(String content, boolean over, int promptTokens, int completionTokens, int totalTokens) {
        FreeMessage freeMessage = new FreeMessage();
        freeMessage.setOver(over);
        freeMessage.setContent(content);
        freeMessage.setPromptTokens(promptTokens);
        freeMessage.setCompletionTokens(completionTokens);
        freeMessage.setTotalTokens(totalTokens);
        return freeMessage;
    }
}
