package com.guoshengkai.litechatgpt.entity;

import com.guoshengkai.litechatgpt.core.annotation.po.ID;
import com.guoshengkai.litechatgpt.core.annotation.po.TableName;
import com.guoshengkai.litechatgpt.core.beans.PO;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@Getter
@Setter
@TableName(name = "USER_CHAT")
public class UserChat extends PO {

    @ID
    private int id;

    private int userId;

    private String name;

    private String avatar;

    private String lastContent;

    private Date lastTime;

    private String description;

    private String systemPrompt;

    private String model;

    private int contentRows;

    private List<String> plugin;

}
