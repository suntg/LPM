package com.example.lpm.v1.strategy;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.example.lpm.v1.constant.ProxyIpType;

@Deprecated
@Component
public class ProxyStrategyFactory {

    private Map<ProxyIpType, ProxyStrategy> strategies;

    @Autowired
    public ProxyStrategyFactory(Set<ProxyStrategy> strategySet) {
        createStrategy(strategySet);
    }

    public ProxyStrategy findStrategy(ProxyIpType strategyName) {
        return strategies.get(strategyName);
    }

    private void createStrategy(Set<ProxyStrategy> strategySet) {
        strategies = new HashMap<>();
        strategySet.forEach(strategy -> strategies.put(strategy.getStrategyName(), strategy));
    }
}
