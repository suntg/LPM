package com.example.lpm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_used_port")
public class UsedPortDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long serverId;

    private Integer serverPort;

    private Long ipAddrId;

    private LocalDateTime createTime;
}
