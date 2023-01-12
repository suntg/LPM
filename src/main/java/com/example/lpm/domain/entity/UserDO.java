package com.example.lpm.domain.entity;

import java.util.Date;

import com.baomidou.mybatisplus.annotation.*;

import lombok.Data;

/**
 * 用户表
 */
@Data
@TableName("t_user")
public class UserDO {

    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 登录账户（手机号）
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 绑定的服务器IP
     */
    private String ip;

    /**
     * 账号激活时间
     */
    private Date activationTime;

    /**
     * 账户过期时间
     */
    private Date expireTime;

    /**
     * 用户类型 （1 公司用户、2 C端用户）
     */
    private Integer userType;

    /**
     * 状态 (1 激活，2 冻结)
     */
    private Integer state;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private Date createTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private Date updateTime;

    /**
     * 创建者
     */
    private String creator;

    /**
     * 更新者
     */
    private String updater;

}
