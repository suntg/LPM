package com.example.lpm.domain.vo;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class LoginVO {

    // @IsMobile
    private String username;

    @NotNull(message = "密码不能为空")
    private String password;

    private String captcha;
}
