package com.example.lpm.v1.domain.request;

import com.example.lpm.v1.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
public class UpdateProxyIpRequest {

    private String ip;

    private Integer status;

    private ProxyIpType proxyIpType;

    private String fileType;

    private String fileFlag;

}
