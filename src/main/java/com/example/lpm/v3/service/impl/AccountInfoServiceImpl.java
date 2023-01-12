package com.example.lpm.v3.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.AccountInfoDO;
import com.example.lpm.v3.mapper.AccountInfoMapper;
import com.example.lpm.v3.service.AccountInfoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountInfoServiceImpl extends ServiceImpl<AccountInfoMapper, AccountInfoDO> implements AccountInfoService {

    private final AccountInfoMapper accountInfoMapper;

    @Override
    public List<AccountInfoDO> getAccountsByParam(String proxyIpType, String zone, String remark) {

        return accountInfoMapper.selectList(new QueryWrapper<AccountInfoDO>().lambda()
                .eq(AccountInfoDO::getStatus, 1)
                .eq(StringUtils.isNotBlank(proxyIpType), AccountInfoDO::getTypeName, proxyIpType)
                .eq(StringUtils.isNotBlank(zone), AccountInfoDO::getZone, zone)
                .like(StringUtils.isNotBlank(remark), AccountInfoDO::getRemark, remark)
        );

    }

    @Override
    public List<AccountInfoDO> getAllAccount() {
        return accountInfoMapper.selectList(null);
    }

    @Override
    public int UpdateAccount(AccountInfoDO accountInfoDO) {
        return accountInfoMapper.updateById(accountInfoDO);
    }
}
