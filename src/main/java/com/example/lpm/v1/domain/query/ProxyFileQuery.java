package com.example.lpm.v1.domain.query;

import com.example.lpm.v1.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
public class ProxyFileQuery {

    private ProxyIpType proxyIpType;

    private String fileType;

    private String fileFlag;

}
