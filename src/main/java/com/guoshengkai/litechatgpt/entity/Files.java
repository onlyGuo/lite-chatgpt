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
@TableName(name = "FILES")
public class Files extends PO {
    @ID
    private int id;

    @FieldName(name = "USER_ID")
    private int userId;

    @FieldName(name = "FILE_KEY")
    private String fileKey;

    @FieldName(name = "CREATE_TIME")
    private Date createTime;

    @FieldName("USE_COUNT")
    private int useCount;
}
