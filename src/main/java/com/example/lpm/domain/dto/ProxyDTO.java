package com.example.lpm.domain.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Deprecated
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyDTO {

    private Integer port;

    private String ip;

    private List ports;
}
