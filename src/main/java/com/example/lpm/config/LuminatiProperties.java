package com.example.lpm.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Data;

@Data
@Component
@ConfigurationProperties(prefix = "luminati")
public class LuminatiProperties {

    private String proxyHost;

    private Integer proxyPort;

    private String proxyUsername;

    private String proxyPassword;

    private String testUrl;

    private String socksUsername;

    private String socksPassword;

    private String customer;

    private String country;

    private String zone;

}
