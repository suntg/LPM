package com.example.lpm.v1.domain.request;

import com.example.lpm.v1.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
public class CheckIpSurvivalRequest {

    private String ip;

    private ProxyIpType proxyIpType;

    private String xLuminatiIp;

    private String username;

    private String password;

    private String zone;

    private String server;

    private String serverPort;

}
