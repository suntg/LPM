package com.example.lpm.v1.controller;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.lpm.v1.domain.entity.OperationLogDO;
import com.example.lpm.v1.domain.query.OperationQuery;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.vo.PageVO;
import com.example.lpm.v1.service.OperationLogService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "operateLog")
@Slf4j
@RestController
@RequestMapping("/operateLog")
public class OperationLogController {

    @Resource
    private OperationLogService operationLogService;

    @Operation(summary = "分页查询")
    @GetMapping("listPage")
    public PageVO<OperationLogDO> listPage(OperationQuery operationQuery, PageQuery pageQuery) {
        return operationLogService.listPage(operationQuery, pageQuery);
    }
}
