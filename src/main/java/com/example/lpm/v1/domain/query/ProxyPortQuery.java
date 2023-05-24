package com.example.lpm.v1.domain.query;

import com.example.lpm.v1.constant.ProxyIpType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
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
