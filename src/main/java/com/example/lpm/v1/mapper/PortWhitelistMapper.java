package com.example.lpm.v1.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v1.domain.entity.PortWhitelistDO;

@Mapper
public interface PortWhitelistMapper extends BaseMapper<PortWhitelistDO> {}