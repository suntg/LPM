package com.example.lpm.v3.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v3.domain.entity.RolaProxyPortDO;

@Mapper
public interface RolaProxyPortMapper extends BaseMapper<RolaProxyPortDO> {

    List<RolaProxyPortDO> listPorts(@Param("proxyPort") Integer proxyPort);

    int deleteByPrimaryKey(Long id);

    /*int insert(RolaProxyPortDO record);*/

    int insertSelective(RolaProxyPortDO record);

    RolaProxyPortDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolaProxyPortDO record);

    int updateByPrimaryKey(RolaProxyPortDO record);

}
