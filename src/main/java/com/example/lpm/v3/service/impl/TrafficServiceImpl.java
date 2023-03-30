package com.example.lpm.v3.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.TrafficDO;
import com.example.lpm.v3.domain.query.TrafficStatisticQuery;
import com.example.lpm.v3.mapper.TrafficMapper;
import com.example.lpm.v3.service.TrafficService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TrafficServiceImpl extends ServiceImpl<TrafficMapper, TrafficDO> implements TrafficService {

    @Resource
    private TrafficMapper trafficMapper;

    @Override
    public int report(TrafficDO record) {
        return trafficMapper.insert(record);
    }

    @Override
    public Long statistic(TrafficStatisticQuery trafficStatisticQuery) {

        QueryWrapper<TrafficDO> queryWrapper = new QueryWrapper<>();
        queryWrapper.select(" sum(bytes) as bytes ");
        queryWrapper.lambda().eq(TrafficDO::getUsername, trafficStatisticQuery.getUsername())
                .ge(ObjectUtil.isNotNull(trafficStatisticQuery.getStartTime()), TrafficDO::getCreateTime,
                        trafficStatisticQuery.getStartTime())
                .le(ObjectUtil.isNotNull(trafficStatisticQuery.getEndTime()), TrafficDO::getCreateTime,
                        trafficStatisticQuery.getEndTime());
        TrafficDO trafficDO = trafficMapper.selectOne(queryWrapper);
        if (trafficDO == null) {
            return 0L;
        }
        return trafficDO.getBytes();
    }

    @Override
    public int deleteByPrimaryKey(Long id) {
        return trafficMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(TrafficDO record) {
        return trafficMapper.insert(record);
    }

    @Override
    public int insertSelective(TrafficDO record) {
        return trafficMapper.insertSelective(record);
    }

    @Override
    public TrafficDO selectByPrimaryKey(Long id) {
        return trafficMapper.selectByPrimaryKey(id);
    }

    @Override
    public int updateByPrimaryKeySelective(TrafficDO record) {
        return trafficMapper.updateByPrimaryKeySelective(record);
    }

    @Override
    public int updateByPrimaryKey(TrafficDO record) {
        return trafficMapper.updateByPrimaryKey(record);
    }

}
