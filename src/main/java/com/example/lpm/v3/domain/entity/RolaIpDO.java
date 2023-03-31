package com.example.lpm.v3.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

@Data
@TableName("t_rola_ip")
public class RolaIpDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ip;

    private String country;

    private String region;

    private String city;

    private String postalCode;

    private String tz;

    private String fileType;

    private String fileFlag;

    private Integer status;

    private String risk;

    private String riskEnglish;

    private Integer score;

    private String source;

    private Integer useNumber;

    private LocalDateTime lastUseTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

}
