package com.example.lpm.domain.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class RolaIpRequest implements Serializable {

    private Long number;

    private String country = "us";

    private String state;

    private String city;

    private Integer proxyPort;

    private String name;

    private Integer flag;

}
