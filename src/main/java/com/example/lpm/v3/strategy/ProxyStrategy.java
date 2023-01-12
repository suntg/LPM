package com.example.lpm.v3.strategy;

import com.example.lpm.v3.constant.ProxyIpType;
import com.example.lpm.v3.domain.entity.ProxyIpDO;
import com.example.lpm.v3.domain.query.LuaGetProxyIpQuery;
import com.example.lpm.v3.domain.request.ChangeIpRequest;
import com.example.lpm.v3.domain.request.CheckIpSurvivalRequest;
import com.example.lpm.v3.domain.request.CollectionTaskRequest;
import com.example.lpm.v3.domain.request.StartProxyPortRequest;

public interface ProxyStrategy {

    ProxyIpDO getProxyIp(LuaGetProxyIpQuery luaGetProxyIpQuery);

    /**
     * 检测存活
     */
    void checkIpSurvival(CheckIpSurvivalRequest checkIpSurvivalRequest);

    /**
     * 启动
     */
    boolean startProxyPort(StartProxyPortRequest startProxyPortRequest);

    /**
     * 收集队列
     */
    void addCollectionTask(CollectionTaskRequest collectionTaskRequest);


    /**
     * 获取进度
     */
    void getCollectionProgress();


    void changeProxyIp(ChangeIpRequest changeIpRequest);

    ProxyIpType getStrategyName();

}
