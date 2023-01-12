package com.example.lpm.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.domain.entity.JobDO;
import com.example.lpm.mapper.JobMapper;
import com.example.lpm.service.JobService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JobServiceImpl extends ServiceImpl<JobMapper, JobDO> implements JobService {

}
