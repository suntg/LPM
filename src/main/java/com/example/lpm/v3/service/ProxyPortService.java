package com.example.lpm.v3.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.domain.entity.ProxyPortDO;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.ProxyPortQuery;
import com.example.lpm.v3.domain.request.DeleteProxyPortRequest;

import java.util.List;
@Deprecated
public interface ProxyPortService extends IService<ProxyPortDO> {

    PageVO<ProxyPortDO> listProxyPortsByPage(ProxyPortQuery proxyPortQuery, PageQuery pageQuery);

    void deleteProxyPortByIp(DeleteProxyPortRequest deleteProxyPortRequest);

    void deleteProxyPort(Long id);

    void deleteBatchProxyPorts(List<Long> ids);

    void deleteAllProxyPort();

}
