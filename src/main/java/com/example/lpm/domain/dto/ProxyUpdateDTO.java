package com.example.lpm.domain.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson2.annotation.JSONField;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProxyUpdateDTO {

    private String zone = "zone5";

    private String password = "jekszis5wjgx";

    @JSONField(name = "rotate_session")
    private Boolean rotateSession = Boolean.FALSE;

    @JSONField(name = "pool_size")
    private Integer poolSize = 0;

    private List headers = new ArrayList<>();

    private String preset = "session_long";

    private List ips = new ArrayList<>();

    private List vips = new ArrayList<>();

    private List users = new ArrayList<>();

    @JSONField(name = "multiply_ips")
    private Boolean multiplyIps = Boolean.FALSE;

    @JSONField(name = "multiply_vips")
    private Boolean multiplyVips = Boolean.FALSE;

    @JSONField(name = "multiply_users")
    private Boolean multiplyUsers = Boolean.FALSE;

    private String dns = "remote";

    @JSONField(name = "reverse_lookup_file")
    private String reverseLookupFile = "";

    @JSONField(name = "reverse_lookup_values")
    private String reverseLookupValues = "";

    @JSONField(name = "reverse_lookup_dns")
    private Boolean reverseLookupDns = Boolean.TRUE;

    @JSONField(name = "proxy_connection_type")
    private String proxyConnectionType = "https";

    private List<Map> rules;

    private String ip;

    public ProxyUpdateDTO() {
        rules = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();

        map.put("action_type", "bypass_proxy");
        map.put("trigger_type", "url");
        map.put("url", "\\.(png|jpg|jpeg|svg|mp3|gif|avi|bmp)(#.*|\\?.*)?$");
        Map<String, Boolean> action = new HashMap<>();
        action.put("bypass_proxy", Boolean.TRUE);
        map.put("action", action);

        rules.add(map);
    }
}
