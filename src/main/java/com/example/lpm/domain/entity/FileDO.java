package com.example.lpm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

@Data
@TableName("t_file")
public class FileDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String path;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String xLuminatiIp;

    @TableField(exist = false)
    private String ip;

    @TableField(exist = false)
    private String logContent;

    @TableField(exist = false)
    private Integer logType;
}
