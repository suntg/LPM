package com.example.lpm.v1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v1.domain.entity.PortWhitelistDO;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.query.PortWhitelistQuery;
import com.example.lpm.v1.domain.vo.PageVO;

public interface PortWhitelistService extends IService<PortWhitelistDO> {

    int deleteById(Long id);

    int insert(PortWhitelistDO record);

    int update(PortWhitelistDO record);

    PageVO<PortWhitelistDO> listPage(PortWhitelistQuery operationQuery, PageQuery pageQuery);
}
