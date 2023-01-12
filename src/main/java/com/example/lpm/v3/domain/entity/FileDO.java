package com.example.lpm.v3.domain.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v3.common.BaseEntity;

import lombok.Data;

@Data
@TableName("t_file")
public class FileDO extends BaseEntity {

    private String name;

    private String path;

    @TableField(exist = false)
    private String xLuminatiIp;

    @TableField(exist = false)
    private String ip;

    @TableField(exist = false)
    private String logContent;

    @TableField(exist = false)
    private Integer logType;
}
