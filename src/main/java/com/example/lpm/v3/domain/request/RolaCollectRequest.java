package com.example.lpm.v3.domain.request;

import com.example.lpm.v3.constant.RolaAccessServerEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RolaCollectRequest implements Serializable {

    private String country;

    private String state;

    private String city;

    private Long number;

    /**
     * 用户名前缀
     */
    private String usernamePrefix;

    private String rolaPassword;

    /**
     * rola服务器
     */
    private RolaAccessServerEnum accessServer;

}
