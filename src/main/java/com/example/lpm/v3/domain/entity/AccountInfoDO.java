package com.example.lpm.v3.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_account_info")
public class AccountInfoDO {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String password;

    private String zone;

    private String server;

    private String serverPort;

    private String refreshServer;

    private Integer status;

    private String typeName;

    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

}
