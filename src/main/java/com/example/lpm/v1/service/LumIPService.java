package com.example.lpm.v1.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v1.domain.entity.LumIPDO;
import com.example.lpm.v1.domain.query.FindSocksPortQuery;
import com.example.lpm.v1.domain.request.LumIPActiveRequest;
import com.example.lpm.v1.domain.request.LumIPCollectRequest;
import com.example.lpm.v1.domain.request.RolaIpLockRequest;

public interface LumIPService extends IService<LumIPDO> {

    List<LumIPDO> listByFileFlag(String fileFlag, String fileType);

    void submitIpLock(RolaIpLockRequest rolaIpLockRequest);

    LumIPDO checkIpLock(RolaIpLockRequest rolaIpLockRequest);

    LumIPDO findSocksPort(FindSocksPortQuery findSocksPortQuery);

    LumIPDO checkIpActive(LumIPActiveRequest lumIPActiveRequest);

    void collect(LumIPCollectRequest lumIPCollectRequest);
}
