package com.example.lpm.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.domain.entity.UserDO;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}