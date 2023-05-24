package com.example.lpm.v1.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v1.domain.entity.ProxyPortDO;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.query.ProxyPortQuery;
import com.example.lpm.v1.domain.request.DeleteProxyPortRequest;
import com.example.lpm.v1.domain.vo.PageVO;

@Deprecated
public interface ProxyPortService extends IService<ProxyPortDO> {

    PageVO<ProxyPortDO> listProxyPortsByPage(ProxyPortQuery proxyPortQuery, PageQuery pageQuery);

    void deleteProxyPortByIp(DeleteProxyPortRequest deleteProxyPortRequest);

    void deleteProxyPort(Long id);

    void deleteBatchProxyPorts(List<Long> ids);

    void deleteAllProxyPort();

}
