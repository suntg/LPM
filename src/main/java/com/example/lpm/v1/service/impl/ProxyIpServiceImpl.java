package com.example.lpm.v1.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v1.common.BizException;
import com.example.lpm.v1.constant.ProxyIpType;
import com.example.lpm.v1.constant.RedisKeyConstant;
import com.example.lpm.v1.domain.entity.ProxyIpDO;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.query.ProxyFileQuery;
import com.example.lpm.v1.domain.query.ProxyIpQuery;
import com.example.lpm.v1.domain.request.RolaIpRequest;
import com.example.lpm.v1.domain.request.UpdateProxyIpRequest;
import com.example.lpm.v1.domain.vo.CollectionProgressVO;
import com.example.lpm.v1.mapper.ProxyIpMapper;
import com.example.lpm.v1.service.ProxyIpService;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Service
@Slf4j
@RequiredArgsConstructor
public class ProxyIpServiceImpl extends ServiceImpl<ProxyIpMapper, ProxyIpDO> implements ProxyIpService {

    private final ProxyIpMapper proxyIpMapper;

    private final RedissonClient redissonClient;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public Page<ProxyIpDO> listProxyIpsByPage(PageQuery pageQuery, ProxyIpQuery proxyIpQuery) {

        if (StringUtils.isNotBlank(proxyIpQuery.getCountry())) {
            proxyIpQuery.setCountry(proxyIpQuery.getCountry().toLowerCase());
        }
        if (StringUtils.isNotBlank(proxyIpQuery.getState())) {
            proxyIpQuery.setState(proxyIpQuery.getState().toLowerCase());
        }
        if (StringUtils.isNotBlank(proxyIpQuery.getCity())) {
            proxyIpQuery.setCity(proxyIpQuery.getCity().toLowerCase());
        }

        return this.page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()), new QueryWrapper<ProxyIpDO>()
            .lambda().ne(ProxyIpDO::getStatus, 0)
            .eq(ObjectUtil.isNotNull(proxyIpQuery.getTypeName()), ProxyIpDO::getTypeName, proxyIpQuery.getTypeName())
            .eq(StringUtils.isNotBlank(proxyIpQuery.getCountry()), ProxyIpDO::getCountry, proxyIpQuery.getCountry())
            .eq(StringUtils.isNotBlank(proxyIpQuery.getState()), ProxyIpDO::getRegion, proxyIpQuery.getState())
            .eq(StringUtils.isNotBlank(proxyIpQuery.getCity()), ProxyIpDO::getCity, proxyIpQuery.getCity())
            .likeRight(StringUtils.isNotBlank(proxyIpQuery.getIp()), ProxyIpDO::getIp, proxyIpQuery.getIp())
            .likeRight(StringUtils.isNotBlank(proxyIpQuery.getZipCode()), ProxyIpDO::getPostalCode,
                proxyIpQuery.getZipCode())
            .orderByDesc(ProxyIpDO::getCreateTime));
    }

    @Override
    public CollectionProgressVO getCollectionProgress(ProxyIpType proxyIpType) {

        RAtomicLong currentNum =
            redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_CURRENT + proxyIpType.getTypeName());

        RAtomicLong totalNum =
            redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_TOTAL + proxyIpType.getTypeName());

        String today = DateUtil.today();
        RAtomicLong todayNum =
            redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_TODAY + proxyIpType.getTypeName() + today);

        RBlockingQueue<RolaIpRequest> queue =
            redissonClient.getBlockingQueue(RedisKeyConstant.COLLECTION_TASK_TOPIC + proxyIpType.getTypeName());

        RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_REPEAT);

        RAtomicLong currentFailNum = redissonClient.getAtomicLong(RedisKeyConstant.COLLECTION_TASK_FAIL);

        String error = redisTemplate.opsForValue().get(RedisKeyConstant.COLLECTION_TASK_ERROR);

        CollectionProgressVO collectionProgressVO = new CollectionProgressVO();
        collectionProgressVO.setCurrentNum(currentNum.get());
        collectionProgressVO.setTotalNum(totalNum.get());
        collectionProgressVO.setProxyIpType(proxyIpType);
        collectionProgressVO.setTodayNum(todayNum.get());
        collectionProgressVO.setCompletedNum(collectionProgressVO.getCurrentNum() - queue.size());
        collectionProgressVO.setCurrentFailNum(currentFailNum.get());
        collectionProgressVO.setCurrentRepeatNum(currentRepeatNum.get());
        collectionProgressVO.setError(error);
        return collectionProgressVO;
    }

    @Override
    public List<ProxyIpDO> getProxyIpByFile(ProxyFileQuery proxyFileQuery) {
        List<ProxyIpDO> proxyIpDOList =
            this.proxyIpMapper.selectList(new QueryWrapper<ProxyIpDO>().lambda().ne(ProxyIpDO::getStatus, 0)
                .apply(StringUtils.isNotBlank(proxyFileQuery.getFileType()),
                    "json_contains(file_type, concat('\"" + proxyFileQuery.getFileType() + "\"'))")
                .apply(StringUtils.isNotBlank(proxyFileQuery.getFileFlag()),
                    "json_contains(file_type, concat('\"" + proxyFileQuery.getFileFlag() + "\"'))"));
        if (CollUtil.isNotEmpty(proxyIpDOList)) {
            return proxyIpDOList;
        } else {
            throw new BizException(900, "没有符合要求的数据");
        }
    }

    @Override
    public void updateProxyIp(UpdateProxyIpRequest updateProxyIpRequest) {
        proxyIpMapper.addFileType(updateProxyIpRequest);
        if (StringUtils.isNotBlank(updateProxyIpRequest.getFileFlag())) {
            proxyIpMapper.addFileFlag(updateProxyIpRequest);
        }
    }

}
