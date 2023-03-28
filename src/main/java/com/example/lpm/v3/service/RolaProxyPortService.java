package com.example.lpm.v3.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.domain.entity.RolaProxyPortDO;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.RolaQuery;
import com.example.lpm.v3.domain.request.RolaIpRequest;
import com.example.lpm.v3.domain.request.RolaStartSocksPortRequest;
import com.example.lpm.v3.domain.vo.PageVO;

public interface RolaProxyPortService extends IService<RolaProxyPortDO> {

    /**
     * 代理端口列表
     */
    PageVO<RolaProxyPortDO> listPortsPage(RolaQuery rolaQuery, PageQuery pageQuery);

    /**
     * 删除代理端口，关psp进程
     */
    void deleteProxyPort(Long id);

    void deleteProxyPortByPort(Integer port);

    void deleteBatchSocksPorts(List<Integer> ports);

    void deleteAllProxyPort();

    void deleteProxyPortByIp(String ip);

    void startProxyPort(RolaIpRequest rolaIpRequest) throws Exception;

    /**
     * 更换代理IP
     */
    void changeProxyIp(RolaIpRequest rolaIpRequest) throws Exception;

    boolean startSocksPort(RolaStartSocksPortRequest startSocksPortRequest);
}