package com.example.lpm.v3.domain.request;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;

@Data
public class UpdateProxyIpRequest {

    private String ip;

    private Integer status;

    private ProxyIpType proxyIpType;

    private String fileType;

    private String fileFlag;

}
