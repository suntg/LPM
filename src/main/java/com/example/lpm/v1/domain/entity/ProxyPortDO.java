package com.example.lpm.v1.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v1.common.BaseEntity;
import com.example.lpm.v1.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
@TableName("t_proxy_port")
public class ProxyPortDO extends BaseEntity {

    private String ip;

    private String name;

    private ProxyIpType typeName;

    private Integer proxyPort;

    private LocalDateTime expirationTime;

    @TableField(exist = false)
    private String country;

    @TableField(exist = false)
    private String region;

    @TableField(exist = false)
    private String city;

}
