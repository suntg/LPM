package com.example.lpm.v3.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.domain.entity.RolaIpDO;
import com.example.lpm.v3.domain.query.FindSocksPortQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.RolaQuery;
import com.example.lpm.v3.domain.request.RolaIpActiveRequest;
import com.example.lpm.v3.domain.request.RolaIpLockRequest;
import com.example.lpm.v3.domain.request.RolaIpRequest;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.domain.vo.RolaProgressVO;

public interface RolaIpService extends IService<RolaIpDO> {

    /**
     * 收集代理IP
     */
    void collect(RolaIpRequest rolaIpRequest);

    void collectV2(RolaIpRequest rolaIpRequest);

    void phoneCollect(RolaIpRequest rolaIpRequest);

    void endCollect();

    void pauseCollect();

    RolaProgressVO collectProgress();

    PageVO<RolaIpDO> listRolaIpsPage(RolaQuery rolaQuery, PageQuery pageQuery);
    PageVO<RolaIpDO> listFilesPage(RolaQuery rolaQuery, PageQuery pageQuery);

    void deleteFile(Long id);
    RolaIpDO findSocksPort(FindSocksPortQuery findSocksPortQuery);

    void submitIpLock(RolaIpLockRequest rolaIpLockRequest);

    RolaIpDO checkIpLock(RolaIpLockRequest rolaIpLockRequest);

    RolaIpDO checkIpActive(RolaIpActiveRequest rolaIpActiveRequest) throws Exception;

    List<RolaIpDO> listByFileFlag(String fileFlag, String fileType);
}