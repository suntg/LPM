package com.example.lpm.v3.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v3.common.BaseEntity;
import lombok.Data;

@Data
@TableName("t_operation_log")
public class OperationLogDO extends BaseEntity {

    private String requestUri;

    private String ip;

}
