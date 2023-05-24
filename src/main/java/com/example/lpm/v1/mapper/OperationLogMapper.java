package com.example.lpm.v1.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v1.domain.entity.OperationLogDO;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogDO> {}
