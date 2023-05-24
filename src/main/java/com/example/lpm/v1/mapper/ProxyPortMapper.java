package com.example.lpm.v1.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v1.domain.entity.ProxyPortDO;
import com.example.lpm.v1.domain.query.ProxyPortQuery;

@Deprecated
@Mapper
public interface ProxyPortMapper extends BaseMapper<ProxyPortDO> {

    List<ProxyPortDO> listProxyPorts(ProxyPortQuery proxyPortQuery);
}
