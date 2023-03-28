package com.example.lpm.v3.domain.vo;

import lombok.Data;

@Data
public class RolaProgressVO {

    private Long currentNum;

    private Long currentFailNum;

    private Long currentRepeatNum;

    private Long totalNum;

    private Long todayNum;

    private Long completedNum;

    private String error;

}
