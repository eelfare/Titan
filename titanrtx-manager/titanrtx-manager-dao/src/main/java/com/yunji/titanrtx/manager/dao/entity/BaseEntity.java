package com.yunji.titanrtx.manager.dao.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class BaseEntity implements Serializable {

    protected Integer id;

    protected Integer deleted;

    protected Date createTime;

    protected Date updateTime;

}
