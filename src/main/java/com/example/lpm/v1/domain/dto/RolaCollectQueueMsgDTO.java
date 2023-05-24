package com.example.lpm.v1.domain.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class RolaCollectQueueMsgDTO implements Serializable {

    private String username;

    private String rolaApiIp;

    private String rolaAccessServer;

}
