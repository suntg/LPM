package com.example.lpm.v3.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v3.common.BaseEntity;

@TableName("t_port_whitelist")
public class PortWhitlistDO extends BaseEntity {

    private Integer port;

}
