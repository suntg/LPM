package com.example.lpm.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.ejlchina.okhttps.HTTP;
import com.ejlchina.okhttps.HttpResult;
import com.ejlchina.okhttps.jackson.JacksonMsgConvertor;
import com.example.lpm.domain.vo.JobInfoVO;
import com.example.lpm.service.XxlJobService;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class XxlJobServiceImpl implements XxlJobService {

    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    @Override
    public void updateById(Integer id, String scheduleConf, String executorParam) {
        HTTP http = HTTP.builder().baseUrl(adminAddresses).build();
        Map<String, Object> params = new HashMap<>();
        params.put("id", id);
        params.put("scheduleConf", scheduleConf);
        if (CharSequenceUtil.isNotBlank(executorParam)) {
            params.put("executorParam", executorParam);
        }
        HttpResult result = http.sync("/jobinfo/updateById").addBodyPara(params).post();
        log.info("jobinfo update result : {}", result);
    }

    @Override
    public void startById(Integer id) {
        HTTP http = HTTP.builder().baseUrl(adminAddresses).build();
        HttpResult result = http.sync("/jobinfo/startById").addBodyPara("id", id).post();
        log.info("start result : {}", result);
    }

    @Override
    public void stopById(Integer id) {
        HTTP http = HTTP.builder().baseUrl(adminAddresses).build();
        HttpResult result = http.sync("/jobinfo/stopById").addBodyPara("id", id).post();
        log.info("stop result : {}", result);
    }

    @Override
    public List<JobInfoVO> listJob() {
        HTTP http = HTTP.builder().baseUrl(adminAddresses).build();
        HttpResult result = http.sync("/jobinfo/listJob").get();
        JSONObject jsonObject = JSON.parseObject(result.getBody().toString());
        JSONArray jsonArray = (JSONArray)jsonObject.get("data");

        List<JobInfoVO> jobInfoVOList = new ArrayList<>();

        for (Object o : jsonArray) {
            JSONObject j = (JSONObject)o;
            JobInfoVO jobInfoVO = new JobInfoVO();
            jobInfoVO.setId(j.getLong("id"));
            jobInfoVO.setExecutorPararm(j.getString("executorParam"));

            String scheduleConf = j.getString("scheduleConf");
            String tmp = CharSequenceUtil.split(scheduleConf, " ").get(1);
            jobInfoVO.setIntervalMinute(Integer.valueOf(CharSequenceUtil.split(tmp, "/").get(1)));

            jobInfoVOList.add(jobInfoVO);
        }
        return jobInfoVOList;
    }

    @Override
    public void triggerById(Integer id) {
        HTTP http = HTTP.builder().baseUrl(adminAddresses).build();
        HttpResult result = http.sync("/jobinfo/trigger").addBodyPara("id", id).post();
        log.info("trigger result : {}", result);
    }

    @Override
    public String logPageList(Integer id) {
        HTTP http = HTTP.builder().baseUrl(adminAddresses).addMsgConvertor(new JacksonMsgConvertor()).build();
        HttpResult result = http.sync("/joblog/pageListNoAuth").addUrlPara("jobId", id).addUrlPara("jobGroup", 2)
            .addUrlPara("logStatus", -1).get();
        JSONArray jsonArray = JSON.parseArray(result.getBody().toString());
        return jsonArray.toString();
    }

}
