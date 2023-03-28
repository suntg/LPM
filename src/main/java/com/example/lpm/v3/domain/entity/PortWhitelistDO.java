package com.example.lpm.v3.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v3.common.BaseEntity;
import lombok.Data;

@Data
@TableName("t_port_whitelist")
public class PortWhitelistDO extends BaseEntity {

    private Integer port;

}
