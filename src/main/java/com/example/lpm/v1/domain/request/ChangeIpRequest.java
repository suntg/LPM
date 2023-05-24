package com.example.lpm.v1.domain.request;

import java.io.Serializable;

import com.example.lpm.v1.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
public class ChangeIpRequest implements Serializable {

    private ProxyIpType proxyIpType = ProxyIpType.ROLA;

    private String country = "us";

    private String state;

    private String city;

    private Integer proxyPort;

    private String name;

    private String proxyUsername = "hotkingda";

    private String proxyPassword = "209209us";

}
