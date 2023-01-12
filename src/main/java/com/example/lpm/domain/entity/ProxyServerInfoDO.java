package com.example.lpm.domain.entity;

import java.time.LocalDateTime;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import lombok.Data;

@Data
@TableName("t_proxy_server_info")
public class ProxyServerInfoDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String serverIp;

    private Integer apiPort;

    private Integer portNumLimit;

    private Integer usedPortNum;

    private Integer state;

    private LocalDateTime createTime;

}
