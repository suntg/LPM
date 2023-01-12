package com.example.lpm.v3.domain.request;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;

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
