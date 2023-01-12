package com.example.lpm.domain.dto;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyDelayDTO implements Serializable {

    private Long serverId;

    private Integer serverPort;

}
