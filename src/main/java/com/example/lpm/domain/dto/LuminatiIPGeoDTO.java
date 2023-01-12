package com.example.lpm.domain.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LuminatiIPGeoDTO {
    private String city;

    private String region;

    @JsonProperty("region_name")
    @JSONField(name = "region_name")
    private String regionName;

    @JsonProperty("postal_code")
    @JSONField(name = "postal_code")
    private String postalCode;

    private String latitude;

    private String longitude;

    private String tz;

    @JsonProperty("lum_city")
    @JSONField(name = "lum_city")
    private String lumCity;

    @JsonProperty("lum_region")
    @JSONField(name = "lum_region")
    private String lumRegion;
}