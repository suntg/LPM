package com.example.lpm.v1.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v1.common.BaseEntity;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_operation_log")
public class OperationLogDO extends BaseEntity {

    private String requestUri;

    private String ip;

    private String deviceName;

    private String deviceInfo;

    private String city;

    private String country;

    private String region;

    /**
     * 来源 1：页面，2：手机
     */
    private Integer source;
}
