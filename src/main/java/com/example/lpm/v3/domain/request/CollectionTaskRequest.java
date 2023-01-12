package com.example.lpm.v3.domain.request;

import java.io.Serializable;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;

@Data
public class CollectionTaskRequest implements Serializable {

    private ProxyIpType proxyIpType;

    private Long number;

    private String country = "us";

    private String state;

    private String city;

}
