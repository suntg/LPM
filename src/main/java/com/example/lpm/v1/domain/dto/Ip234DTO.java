package com.example.lpm.v1.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Ip234DTO {

    private String ip;
    private String organization;
    private String asn;
    private String network;
    @JsonProperty("country_cn")
    private String countryCn;
    private String country;
    @JsonProperty("country_code")
    private String countryCode;
    @JsonProperty("city_cn")
    private String cityCn;
    private String city;
    @JsonProperty("continent_cn")
    private String continentCn;
    private String continent;
    @JsonProperty("continent_code")
    private String continentCode;
    private String postal;
    private String latitude;
    private String longitude;
    private String timezone;
    @JsonProperty("metro_code")
    private String metroCode;
    @JsonProperty("region_code")
    private String regionCode;
    @JsonProperty("region_cn")
    private String regionCn;
    private String region;

}
