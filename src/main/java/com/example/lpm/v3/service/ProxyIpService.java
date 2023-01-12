package com.example.lpm.v3.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.constant.ProxyIpType;
import com.example.lpm.v3.domain.entity.ProxyIpDO;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.ProxyFileQuery;
import com.example.lpm.v3.domain.query.ProxyIpQuery;
import com.example.lpm.v3.domain.request.UpdateProxyIpRequest;
import com.example.lpm.v3.domain.vo.CollectionProgressVO;

public interface ProxyIpService extends IService<ProxyIpDO> {

    Page<ProxyIpDO> listProxyIpsByPage(PageQuery pageQuery, ProxyIpQuery proxyIpQuery);

    CollectionProgressVO getCollectionProgress(ProxyIpType proxyIpType);

    List<ProxyIpDO> getProxyIpByFile(ProxyFileQuery proxyFileQuery);

    void updateProxyIp(UpdateProxyIpRequest updateProxyIpRequest);

}
