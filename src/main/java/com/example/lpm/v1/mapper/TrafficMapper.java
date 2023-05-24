package com.example.lpm.v1.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v1.domain.entity.TrafficDO;

@Mapper
public interface TrafficMapper extends BaseMapper<TrafficDO> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(TrafficDO record);

    TrafficDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrafficDO record);

    int updateByPrimaryKey(TrafficDO record);
}