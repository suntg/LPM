package com.example.lpm.v1.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v1.domain.entity.AccountInfoDO;

public interface AccountInfoService extends IService<AccountInfoDO> {

    List<AccountInfoDO> getAccountsByParam(String proxyIpType, String zone, String remark);

    List<AccountInfoDO> getAllAccount();

    int UpdateAccount(AccountInfoDO accountInfoDO);

}
