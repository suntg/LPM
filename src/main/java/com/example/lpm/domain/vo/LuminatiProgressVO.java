package com.example.lpm.domain.vo;

import lombok.Data;

@Data
public class LuminatiProgressVO {

    private Long currentNum;

    private Long currentFailNum;

    private Long currentRepeatNum;

    private Long totalNum;

    private Long todayNum;

    private Long completedNum;

    private String error;

}
