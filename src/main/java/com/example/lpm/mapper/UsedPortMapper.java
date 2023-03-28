package com.example.lpm.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.domain.entity.UsedPortDO;

@Deprecated
@Mapper
public interface UsedPortMapper extends BaseMapper<UsedPortDO> {

    Integer selectMaxPort(@Param("serverId") Long serverId);

}
