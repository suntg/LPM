package com.example.lpm.domain.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LuminatiIPAsnDTO {
    private String asnum;

    @JsonProperty("org_name")
    @JSONField(name = "org_name")
    private String orgName;
}