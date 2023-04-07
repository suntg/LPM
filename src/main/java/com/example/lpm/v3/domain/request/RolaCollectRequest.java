package com.example.lpm.v3.domain.request;

import com.example.lpm.v3.constant.RolaAccessServerEnum;
import lombok.Data;

import java.io.Serializable;

@Data
public class RolaCollectRequest implements Serializable {

    private String country;

    private String state;

    private String city;

    private Long number;

    private String usernamePrefix;

    private RolaAccessServerEnum accessServer;


}
