package com.example.lpm.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.domain.entity.UserDO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<UserDO> {

}