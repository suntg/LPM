package com.example.lpm.v3.service.impl;

import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import com.example.lpm.v3.domain.query.OperationQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.mapper.OperationLogMapper;
import com.example.lpm.v3.service.OperationLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLogDO> implements OperationLogService {

    @Resource
    private OperationLogMapper operationLogMapper;

    @Override
    public PageVO<OperationLogDO> listPage(OperationQuery operationQuery, PageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<OperationLogDO> operationLogDOList = operationLogMapper.selectList(new QueryWrapper<OperationLogDO>().lambda().eq(ObjectUtil.isNotEmpty(operationQuery.getIp()), OperationLogDO::getIp, operationQuery.getIp()).like(ObjectUtil.isNotEmpty(operationQuery.getDeviceName()), OperationLogDO::getDeviceName, operationQuery.getDeviceName()).like(ObjectUtil.isNotEmpty(operationQuery.getDeviceInfo()), OperationLogDO::getDeviceInfo, operationQuery.getDeviceInfo()).like(ObjectUtil.isNotEmpty(operationQuery.getRequestUri()), OperationLogDO::getRequestUri, operationQuery.getRequestUri()).like(ObjectUtil.isNotEmpty(operationQuery.getCity()), OperationLogDO::getCity, operationQuery.getCity()).like(ObjectUtil.isNotEmpty(operationQuery.getRegion()), OperationLogDO::getRegion, operationQuery.getRegion()).ge(ObjectUtil.isNotNull(operationQuery.getStartCreateTime()), OperationLogDO::getCreateTime, operationQuery.getStartCreateTime()).le(ObjectUtil.isNotNull(operationQuery.getEndCreateTime()), OperationLogDO::getCreateTime, operationQuery.getEndCreateTime()).orderByDesc(OperationLogDO::getCreateTime));
        return new PageVO<>(page.getTotal(), operationLogDOList);
    }

    @Override
    public void record(OperationLogDO operationLogDO) {
        try {
            operationLogDO.setCity(null);
            operationLogDO.setCountry(null);
            operationLogDO.setRegion(null);
            OperationLogDO result = operationLogMapper.selectOne(new QueryWrapper<OperationLogDO>().lambda()
                    .eq(OperationLogDO::getIp, operationLogDO.getIp()).isNotNull(OperationLogDO::getCity)
                    .isNotNull(OperationLogDO::getCountry).isNotNull(OperationLogDO::getCountry).orderByDesc(OperationLogDO::getCreateTime).last(" limit 1"));
            if (result == null || CharSequenceUtil.isBlank(result.getRegion()) || CharSequenceUtil.isBlank(result.getCity())) {
                String ipInfo = HttpUtil.get("https://www.fkcoder.com/ip?ip=" + operationLogDO.getIp());
                JSONObject jsonObject = JSON.parseObject(ipInfo);
                String country = jsonObject.getString("country");
                if (CharSequenceUtil.isNotBlank(country) && !CharSequenceUtil.equals("0", country)) {
                    operationLogDO.setCountry(country);
                }
                String province = jsonObject.getString("province");
                if (CharSequenceUtil.isNotBlank(province) && !CharSequenceUtil.equals("0", province)) {
                    operationLogDO.setRegion(province);
                }
                String city = jsonObject.getString("city");
                if (CharSequenceUtil.isNotBlank(city) && !CharSequenceUtil.equals("0", city)) {
                    operationLogDO.setCity(city);
                }
            } else {
                operationLogDO.setCountry(result.getCountry());
                operationLogDO.setRegion(result.getRegion());
                operationLogDO.setCity(result.getCity());
            }
        } catch (Exception e) {
            log.error("ip查询{}异常:{}", operationLogDO.getIp(), ExceptionUtil.stacktraceToString(e));
        }
        operationLogDO.setCreateTime(LocalDateTime.now());
        operationLogMapper.insert(operationLogDO);
    }
}
