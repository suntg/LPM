package com.example.lpm.domain.query;

import java.util.List;

import lombok.Data;

@Data
public class FindIpQuery {

    private String state;

    private String city;

    private String zipCode;

    private String ip;

    private String country;

    private List<ZipCodeQuery> zipCodeList;

}
