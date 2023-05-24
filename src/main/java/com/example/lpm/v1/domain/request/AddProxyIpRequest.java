package com.example.lpm.v1.domain.request;

import com.example.lpm.v1.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
public class AddProxyIpRequest {

    private Integer number;

    private ProxyIpType proxyIpType;

    private String country;

    private String state;

    private String city;

}
