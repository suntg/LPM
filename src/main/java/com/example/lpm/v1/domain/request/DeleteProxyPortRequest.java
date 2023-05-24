package com.example.lpm.v1.domain.request;

import java.util.List;

import com.example.lpm.v1.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
public class DeleteProxyPortRequest {

    private String server;

    private List<Integer> ports;

    private String ip;

    private ProxyIpType typeName;

}
