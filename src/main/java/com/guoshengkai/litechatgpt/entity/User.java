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
@TableName(name = "USER")
public class User extends PO {

    @ID
    private int id;

    @FieldName(name = "NICKER")
    private String nicker;

    @FieldName(name = "AVATAR")
    private String avatar;

    @FieldName(name = "EMAIL")
    private String email;

    @FieldName(name = "MOBILE")
    private String mobile;

    @FieldName(name = "PASSWORD")
    private String password;

    @FieldName(name = "CREATE_TIME")
    private Date createTime;

    /**
     * 0 == 正常, 1 == 禁用
     */
    @FieldName(name = "STATUS")
    private int status;

    @FieldName(name = "LEVEL")
    private int level;

    @FieldName(name = "EXP")
    private int exp;

    @FieldName(name = "VIP_LEVEL")
    private int vipLevel;

    @FieldName(name = "VIP_EXPIRE_TIME")
    private Date vipExpireTime;
}
