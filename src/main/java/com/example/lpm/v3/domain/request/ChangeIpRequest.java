package com.example.lpm.v3.domain.request;

import com.example.lpm.v3.constant.ProxyIpType;
import lombok.Data;

import java.io.Serializable;

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
