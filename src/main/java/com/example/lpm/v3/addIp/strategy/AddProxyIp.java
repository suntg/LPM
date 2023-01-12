package com.example.lpm.v3.addIp.strategy;

import com.example.lpm.v3.domain.request.CollectionTaskRequest;

public interface AddProxyIp {

    /**
     * lua添加IP 合并
     */
    void addProxyIpTask(CollectionTaskRequest collectionTaskRequest);
}
