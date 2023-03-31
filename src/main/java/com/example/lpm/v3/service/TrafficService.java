package com.example.lpm.v3.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.domain.entity.TrafficDO;
import com.example.lpm.v3.domain.query.TrafficStatisticQuery;
import com.example.lpm.v3.domain.vo.TrafficVO;

import java.util.List;

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
