package com.example.lpm.domain.query;

import java.util.List;

import lombok.Data;

@Data
public class FindSocksPortQuery {

    private String country;

    private String state;

    private String city;

    private String zipCode;

    private String fileType;

    private String ip;

    private List<ZipCodeQuery> zipCodeList;

}
