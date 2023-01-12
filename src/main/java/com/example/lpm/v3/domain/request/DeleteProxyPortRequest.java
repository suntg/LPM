package com.example.lpm.v3.domain.request;

import java.util.List;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;

@Data
public class DeleteProxyPortRequest {

    private String server;

    private List<Integer> ports;

    private String ip;

    private ProxyIpType typeName;

}
