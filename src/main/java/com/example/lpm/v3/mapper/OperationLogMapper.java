package com.example.lpm.v3.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLogDO> {
}
