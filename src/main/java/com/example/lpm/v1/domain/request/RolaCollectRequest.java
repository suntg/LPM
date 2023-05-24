package com.example.lpm.v1.domain.request;

import java.io.Serializable;

import com.example.lpm.v1.constant.RolaAccessServerEnum;

import lombok.Data;

@Data
public class RolaCollectRequest implements Serializable {

    private String country;

    private String state;

    private String city;

    private Long number;

    private String username;

    private String rolaPassword;

    /**
     * rola服务器
     */
    private RolaAccessServerEnum accessServer;

}
