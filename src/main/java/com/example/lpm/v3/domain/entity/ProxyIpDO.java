package com.example.lpm.v3.domain.entity;

import java.util.List;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import com.example.lpm.v3.common.BaseEntity;
import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Deprecated
@EqualsAndHashCode(callSuper = true)
@Data
@TableName(value = "t_proxy_ip", autoResultMap = true)
public class ProxyIpDO extends BaseEntity {

    private String ip;

    private String country;

    private String region;

    private String city;

    private String postalCode;

    private String timezone;

    private String xLuminatiIp;

    private String risk;

    private String riskEnglish;

    private Integer score;

    private ProxyIpType typeName;

    private Integer status;

    @TableField(value="file_type",typeHandler = JacksonTypeHandler.class)
    private List<String> fileType;

    @TableField(value="file_flag",typeHandler = JacksonTypeHandler.class)
    private List<String> fileFlag;
}
