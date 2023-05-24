package com.example.lpm.v1.domain.vo;

import java.util.List;

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

    private Long successNum;
    private Long failNum;
    private Long duplicateNum;
    private List<Object> rolaCollectResultDTOList;

}
