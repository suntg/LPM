package com.example.lpm.v3.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

@Data
@TableName("t_rola_proxy_port")
public class RolaProxyPortDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Integer proxyPort;

    private String rolaIp;

    private String name;

    private LocalDateTime expirationTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String country;

    @TableField(exist = false)
    private String region;

    @TableField(exist = false)
    private String city;

}
