package com.example.lpm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_lum_ip_addr")
public class IpAddrDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ip;

    private String country;

    private String region;

    private String city;

    private String postalCode;

    @TableField(value = "x_luminati_ip")
    private String xLuminatiIp;

    private Integer state;

    private String remark;

    private Integer heartbeatQuantity;

    private LocalDateTime lastHeartbeatTime;

    private LocalDateTime createTime;

    private Integer useState;

}
