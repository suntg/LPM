package com.example.lpm.v3.domain.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RolaCollectQueueMsgDTO implements Serializable {


    private String username;

    private String rolaApiIp;

    private String rolaAccessServer;

}
