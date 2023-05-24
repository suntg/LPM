package com.example.lpm.v1.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v1.common.BaseEntity;

import lombok.Data;

@Data
@TableName("t_file_ip")
public class FileIpDO extends BaseEntity {

    private Long fileId;

    private String ip;

    private String xLuminatiIp;

}
