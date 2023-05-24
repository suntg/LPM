package com.example.lpm.v1.controller;

import org.springframework.web.bind.annotation.*;

import com.example.lpm.v1.domain.entity.PortWhitelistDO;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.query.PortWhitelistQuery;
import com.example.lpm.v1.domain.vo.PageVO;
import com.example.lpm.v1.service.PortWhitelistService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "PortWhitelist")
@Slf4j
@RestController
@RequestMapping("/portWhitelist")
@RequiredArgsConstructor
public class PortWhitelistController {

    private final PortWhitelistService portWhitelistService;

    @Operation(summary = "分页查询")
    @GetMapping("listPage")
    public PageVO<PortWhitelistDO> listPage(PortWhitelistQuery portWhitelistQuery, PageQuery pageQuery) {
        return portWhitelistService.listPage(portWhitelistQuery, pageQuery);
    }

    @PostMapping("deleteById")
    public void deleteById(Long id) {
        portWhitelistService.deleteById(id);
    }

    @PostMapping("insert")
    public void insert(@RequestBody PortWhitelistDO record) {
        portWhitelistService.insert(record);
    }

    @PostMapping("update")
    public void update(@RequestBody PortWhitelistDO record) {
        portWhitelistService.update(record);
    }
}
