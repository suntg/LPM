package com.example.lpm.v1.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v1.domain.entity.OperationLogDO;
import com.example.lpm.v1.domain.query.OperationQuery;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.vo.PageVO;

public interface OperationLogService extends IService<OperationLogDO> {

    PageVO<OperationLogDO> listPage(OperationQuery operationQuery, PageQuery pageQuery);

    void record(OperationLogDO operationLogDO);

}
