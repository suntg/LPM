package com.example.lpm.v1.domain.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class LumIPActiveRequest implements Serializable {

    private String socksAddressIp;

    private String xLuminatiIp;

    private String customerUsername;

    private String zoneUsername;

    private String zonePassword;

}
