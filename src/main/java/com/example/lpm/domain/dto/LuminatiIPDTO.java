package com.example.lpm.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LuminatiIPDTO {

    private String ip;

    private String country;

    private LuminatiIPAsnDTO asn;

    private LuminatiIPGeoDTO geo;

    /**
     * 0：ip不同，1：ip相同
     */
    private Integer state;

}
