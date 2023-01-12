package com.example.lpm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_luminati_ip")
public class LuminatiIPDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String ip;

    private String country;

    private String region;

    private String city;

    private String postalCode;

    @TableField(value = "x_luminati_ip")
    private String xLuminatiIp;

    private LocalDateTime createTime;

    private String socksPort;

    private String socksUsername;
    private String socksPassword;
    private String customer;
    private String zone;
    private String zonePassword;
    private String proxyUrl;

    @TableField(exist = false)
    private String serverIp = "47.75.52.53";
}
