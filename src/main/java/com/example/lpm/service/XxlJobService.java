package com.example.lpm.service;

import java.util.List;

import com.example.lpm.domain.vo.JobInfoVO;

public interface XxlJobService {
    void updateById(Integer id, String scheduleConf, String executorParam);

    void startById(Integer id);

    void stopById(Integer id);

    List<JobInfoVO> listJob();

    void triggerById(Integer id);

    String logPageList(Integer id);
}
