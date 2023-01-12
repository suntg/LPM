package com.example.lpm.v3.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.constant.ProxyIpType;
import com.example.lpm.v3.domain.entity.AccountInfoDO;

import java.util.List;

public interface AccountInfoService extends IService<AccountInfoDO> {

    List<AccountInfoDO> getAccountsByParam(String proxyIpType, String zone, String remark);


    List<AccountInfoDO> getAllAccount();

    int UpdateAccount(AccountInfoDO accountInfoDO);

}
