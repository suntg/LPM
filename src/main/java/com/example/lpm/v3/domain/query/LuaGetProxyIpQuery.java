package com.example.lpm.v3.domain.query;

import java.util.List;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;

@Data
public class LuaGetProxyIpQuery {

    private ProxyIpType proxyIpType;

    private String state;

    private String city;

    private String zipCode;

    private String ip;

    private String country;

    private List<LuaZipCodeQuery> zipCodeList;


    private String fileType;

}
