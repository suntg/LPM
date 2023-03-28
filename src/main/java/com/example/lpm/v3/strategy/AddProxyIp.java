package com.example.lpm.v3.strategy;

import com.example.lpm.v3.domain.request.CollectionTaskRequest;

@Deprecated
public interface AddProxyIp {

    /**
     * lua添加IP 合并
     */
    void addProxyIpTask(CollectionTaskRequest collectionTaskRequest);
}
