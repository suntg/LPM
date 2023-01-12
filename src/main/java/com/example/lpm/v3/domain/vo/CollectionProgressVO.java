package com.example.lpm.v3.domain.vo;

import com.example.lpm.v3.constant.ProxyIpType;
import lombok.Data;

@Data
public class CollectionProgressVO {

    private ProxyIpType proxyIpType;

    private Long currentNum;

    private Long currentFailNum;

    private Long currentRepeatNum;

    private Long totalNum;

    private Long todayNum;

    private Long completedNum;

    private String error;

}
