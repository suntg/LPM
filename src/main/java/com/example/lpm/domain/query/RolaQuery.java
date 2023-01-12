package com.example.lpm.domain.query;

import lombok.Data;

@Data
public class RolaQuery {

    private Integer proxyPort;

    private String ip;

    private String country;

    private String state;

    private String city;

    private String zipCode;
}
