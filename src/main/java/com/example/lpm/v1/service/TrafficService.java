package com.example.lpm.v1.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v1.domain.entity.TrafficDO;
import com.example.lpm.v1.domain.query.TrafficStatisticQuery;
import com.example.lpm.v1.domain.vo.TrafficVO;

public interface TrafficService extends IService<TrafficDO> {

    int report(TrafficDO record);

    List<TrafficVO> statistic(TrafficStatisticQuery trafficStatisticQuery);

    int deleteByPrimaryKey(Long id);

    int insert(TrafficDO record);

    int insertSelective(TrafficDO record);

    TrafficDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TrafficDO record);

    int updateByPrimaryKey(TrafficDO record);

}
