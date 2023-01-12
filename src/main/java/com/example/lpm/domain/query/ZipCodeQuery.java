package com.example.lpm.domain.query;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class ZipCodeQuery {

    private String state;

    @JsonProperty("zip_code")
    private String zipCode;

    private Double distance;

    private String city;

}
