package com.example.lpm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_job")
public class JobDO {

    private Long id;

    private Integer job;

    private Long targetNum;

    private Long progressNum;

    private Integer state;

    private LocalDateTime createTime;

}
