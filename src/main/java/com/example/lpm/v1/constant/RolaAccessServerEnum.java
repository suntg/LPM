package com.example.lpm.v1.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum RolaAccessServerEnum {

    CHINA("gate8.rola.info:2008"), US("proxyus.rola.info:2000");

    @EnumValue
    @JsonValue
    private final String serverAddr;

    public static RolaAccessServerEnum valueOfServerAddr(String serverAddr) {
        for (RolaAccessServerEnum obj : RolaAccessServerEnum.values()) {
            if (java.util.Objects.equals(obj.serverAddr, serverAddr)) {
                return obj;
            }
        }
        return null;
    }

    public String getServerAddr() {
        return serverAddr;
    }
}
