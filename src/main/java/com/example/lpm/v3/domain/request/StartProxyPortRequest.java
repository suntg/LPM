package com.example.lpm.v3.domain.request;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;

@Data
public class StartProxyPortRequest {

    private String proxyIp;

    private ProxyIpType proxyIpType;

    private Integer proxyPort;

    private String xLuminatiIp;

    private String proxyUsername = "hotkingda";

    private String proxyPassword = "209209us";

    private String username;

    private String password;

    private String zone;

    private String server;

    private String serverPort;

    private String deviceName;

}
