package com.example.lpm.v1.domain.request;

import java.io.Serializable;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class LumIPCollectRequest implements Serializable {

    private String country;

    private String state;

    private String city;

    private Long number;

    private String customerUsername;

    private String zoneUsername;

    private String zonePassword;

}
