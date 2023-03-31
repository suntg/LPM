package com.example.lpm.v3.domain.query;

import lombok.Data;

@Data
public class RolaQuery {

    private Integer proxyPort;

    private String ip;

    private String country;

    private String state;

    private String city;

    private String zipCode;

    private String fileName;
}
