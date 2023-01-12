package com.example.lpm.domain.request;

import java.io.Serializable;

import lombok.Data;

@Data
public class LuminatiCollectIpRequest implements Serializable {

    private Long number;

    private String country = "us";

    private String state;

    private String city;

}
