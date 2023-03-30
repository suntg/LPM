package com.example.lpm.v3.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v3.domain.entity.TrafficDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TrafficMapper extends BaseMapper<TrafficDO> {
    int deleteByPrimaryKey(Long id);

    int insertSelective(TrafficDO record);

    TrafficDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrafficDO record);

    int updateByPrimaryKey(TrafficDO record);
}