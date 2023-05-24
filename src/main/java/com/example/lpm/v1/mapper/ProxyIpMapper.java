package com.example.lpm.v1.mapper;

import org.apache.ibatis.annotations.Mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v1.domain.entity.ProxyIpDO;
import com.example.lpm.v1.domain.request.UpdateProxyIpRequest;

@Deprecated
@Mapper
public interface ProxyIpMapper extends BaseMapper<ProxyIpDO> {

    Integer addFileType(UpdateProxyIpRequest updateProxyIpRequest);

    Integer addFileFlag(UpdateProxyIpRequest updateProxyIpRequest);

}
