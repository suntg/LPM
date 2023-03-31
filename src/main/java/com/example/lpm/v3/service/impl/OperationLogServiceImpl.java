package com.example.lpm.v3.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import com.example.lpm.v3.domain.query.OperationQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.mapper.OperationLogMapper;
import com.example.lpm.v3.service.OperationLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLogDO> implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public PageVO<OperationLogDO> listPage(OperationQuery operationQuery, PageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<OperationLogDO> operationLogDOList = operationLogMapper.selectList(new QueryWrapper<OperationLogDO>().lambda()
                .eq(ObjectUtil.isNotEmpty(operationQuery.getIp()), OperationLogDO::getIp,operationQuery.getIp())
                .like(ObjectUtil.isNotEmpty(operationQuery.getDeviceName()), OperationLogDO::getDeviceName,operationQuery.getDeviceName())
                .like(ObjectUtil.isNotEmpty(operationQuery.getDeviceInfo()), OperationLogDO::getDeviceInfo,operationQuery.getDeviceInfo())
                .like(ObjectUtil.isNotEmpty(operationQuery.getRequestUri()), OperationLogDO::getRequestUri,operationQuery.getRequestUri())
                .ge(ObjectUtil.isNotNull(operationQuery.getStartCreateTime()), OperationLogDO::getCreateTime,
                        operationQuery.getStartCreateTime())
                .le(ObjectUtil.isNotNull(operationQuery.getEndCreateTime()), OperationLogDO::getCreateTime,
                        operationQuery.getEndCreateTime())
                .orderByDesc(OperationLogDO::getCreateTime)
        );
        return new PageVO<>(page.getTotal(), operationLogDOList);
    }
}
