package com.example.lpm.v3.constant;

import org.apache.commons.lang3.StringUtils;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ProxyIpType {

    /**
     * rola
     */
    ROLA("ROLA"),
    /**
     * luminati
     */
    LUMINATI("LUMINATI");

    @EnumValue
    @JsonValue
    private final String typeName;

    // @JsonCreator
    public static ProxyIpType valueOfTypeName(String typeName) {
        for (ProxyIpType type : ProxyIpType.values()) {
            if (StringUtils.equals(type.typeName, typeName)) {
                return type;
            }
        }
        return null;
    }

    public String getTypeName() {
        return typeName;
    }

}
