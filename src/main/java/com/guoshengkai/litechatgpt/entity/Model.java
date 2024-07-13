package com.guoshengkai.litechatgpt.entity;

import com.guoshengkai.litechatgpt.core.annotation.po.FieldName;
import com.guoshengkai.litechatgpt.core.annotation.po.ID;
import com.guoshengkai.litechatgpt.core.annotation.po.TableName;
import com.guoshengkai.litechatgpt.core.beans.PO;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@TableName(name = "MODEL")
public class Model extends PO {

    @ID
    private int id;

    @FieldName(name = "NAME")
    private String name;

    @FieldName(name = "FINAL_NAME")
    private String finalName;

    @FieldName(name = "DESCRIPTION")
    private String description;

}
