package com.example.lpm.v3.domain.query;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class LuaZipCodeQuery {

    private String state;

    @JsonProperty("zip_code")
    private String zipCode;

    private Double distance;

    private String city;

}
