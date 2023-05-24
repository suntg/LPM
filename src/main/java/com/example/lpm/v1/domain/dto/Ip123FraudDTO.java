package com.example.lpm.v1.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Ip123FraudDTO {

    private String risk;

    @JsonProperty("risk_english")
    private String riskEnglish;

    private Integer score;
}
