package com.example.lpm.v3.domain.query;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProxyPortQuery {

    private ProxyIpType typeName;

    private String ip;

    private String country;

    private String state;

    private String city;

    private String zipCode;

}
