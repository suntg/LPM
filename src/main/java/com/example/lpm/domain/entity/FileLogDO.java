package com.example.lpm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

@Data
@TableName("t_file_log")
public class FileLogDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fileId;

    private String content;

    private Integer type;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
