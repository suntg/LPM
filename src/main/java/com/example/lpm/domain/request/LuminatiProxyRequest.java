package com.example.lpm.domain.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class LuminatiProxyRequest {

    private String proxyServer;

    private Integer apiPort;

    private Integer proxyPort;

    private String country;

    private String state;

    private String city;

    private String zipCode;

    @Schema(description = "Luminati  x-luminati-ip")
    @JsonProperty(value = "xLuminatiIp")
    private String xLuminatiIp;

    private String checkIp;

    private Integer timeout;

    @Schema(description = "先删除端口标志，0否，1是，默认是0")
    private Integer deleteProxyPortFlag = 0;

    private String zone;

    private String zonePassword;
}
