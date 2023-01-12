package com.example.lpm.v3.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Ip123InfoDTO {

    private Integer asn;

    private String city;

    private String continent;

    @JsonProperty("continent_code")
    private String continentCode;

    private String country;

    @JsonProperty("country_code")
    private String countryCode;

    private String ip;

    private String latitude;

    private String longitude;

    @JsonProperty("metro_code")
    private String metroCode;

    private String network;

    private String organization;

    private String postal;

    private String region;

    @JsonProperty("region_code")
    private String regionCode;

    private String timezone;

}
