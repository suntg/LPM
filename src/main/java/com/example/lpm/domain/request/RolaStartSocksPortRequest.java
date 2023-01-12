package com.example.lpm.domain.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class RolaStartSocksPortRequest implements Serializable {

    private Integer socksPort;

    private String socksUsername;

    private String socksPassword;

    private String socksAddressIp;

    private String rolaUsername;

    private String rolaPassword;

    private String deviceName;

}
