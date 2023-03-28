package com.example.lpm.v3.domain.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class RolaIpLockRequest implements Serializable {

    private String socksAddressIp;

    private String fileType;

    private String fileFlag;

    private Integer status;

}
