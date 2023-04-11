package com.example.lpm.v3.domain.entity;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("t_rola_config")
public class RolaConfigDO {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String sidUsername;

    private String token;

    private String proxyPassword;
}
