package com.example.lpm.domain.dto;

import java.util.ArrayList;
import java.util.List;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProxyAddDTO {

    private Integer port;

    private String preset = "rotating";

    private String zone = "";

    @JSONField(name = "proxy_type")
    private String proxyType = "persist";

    private String session = "";

    @JSONField(name = "rotate_session")
    private Boolean rotateSession = Boolean.TRUE;

    @JSONField(name = "sticky_ip")
    private Boolean stickyIp = Boolean.FALSE;

    private List headers = new ArrayList<>();
}
