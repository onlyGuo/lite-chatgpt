package com.guoshengkai.litechatgpt.entity;

import com.guoshengkai.litechatgpt.core.annotation.po.FieldName;
import com.guoshengkai.litechatgpt.core.annotation.po.ID;
import com.guoshengkai.litechatgpt.core.annotation.po.TableName;
import com.guoshengkai.litechatgpt.core.beans.PO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@TableName(name = "CHAT_MESSAGE")
public class Message extends PO {

    @ID
    private int id;

    @FieldName(name = "USER_ID")
    private int userId;

    @FieldName(name = "CHAT_ID")
    private int chatId;

    @FieldName(name = "ROLE")
    private String role;

    @FieldName(name = "CONTENT")
    private String content;

    @FieldName(name = "CREATE_TIME")
    private Date createTime;
}
