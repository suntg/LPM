package com.example.lpm.v3.controller;


import com.example.lpm.v3.domain.entity.PortWhitelistDO;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.PortWhitelistQuery;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.service.PortWhitelistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PortWhitelist")
@Slf4j
@RestController
@RequestMapping("/portWhitelist")
@RequiredArgsConstructor
public class PortWhitelistController {


    private PortWhitelistService portWhitelistService;


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
    public void insert(PortWhitelistDO record) {
        portWhitelistService.insert(record);
    }

    @PostMapping("update")
    public void update(PortWhitelistDO record) {
        portWhitelistService.update(record);
    }
}
