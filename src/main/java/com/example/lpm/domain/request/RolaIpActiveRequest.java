package com.example.lpm.domain.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class RolaIpActiveRequest implements Serializable {

    private String socksAddressIp;

    private String rolaUsername;

    private String rolaPassword;

}
