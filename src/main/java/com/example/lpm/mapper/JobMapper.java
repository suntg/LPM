package com.example.lpm.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.domain.entity.JobDO;

@Deprecated
@Mapper
public interface JobMapper extends BaseMapper<JobDO> {

}
