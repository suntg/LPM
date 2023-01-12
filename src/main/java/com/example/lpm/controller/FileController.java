package com.example.lpm.controller;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.web.bind.annotation.*;

import com.example.lpm.domain.dto.FileDTO;
import com.example.lpm.domain.entity.FileIpDO;
import com.example.lpm.domain.entity.FileLogDO;
import com.example.lpm.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.domain.vo.PageVO;
import com.example.lpm.service.FileIpService;
import com.example.lpm.service.FileLogService;
import com.example.lpm.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "File")
@Slf4j
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileService;

    @Resource
    private FileIpService fileIpService;

    @Resource
    private FileLogService fileLogService;

    @Operation(summary = "分页查询File")
    @GetMapping("listFilesPage")
    public PageVO<FileDTO> listFilePage(FileQuery fileQuery, PageQuery pageQuery) {
        return fileService.listPage(fileQuery, pageQuery);
    }

    @Operation(summary = "查询多个File详情")
    @GetMapping("listFiles")
    public List<FileDTO> listFiles(@RequestParam(value = "fileIds") List<Long> fileIds) {
        return fileService.listFiles(fileIds);
    }

    @Operation(summary = "分页查询Log")
    @GetMapping("listLogsPage")
    public PageVO<FileLogDO> listLogsPage(FileQuery fileQuery, PageQuery pageQuery) {
        return fileLogService.listLogsPage(fileQuery, pageQuery);
    }

    @Operation(summary = "查询Log")
    @GetMapping("getLog")
    public FileLogDO getLog(@RequestParam Long id) {
        return fileLogService.getById(id);
    }

    @Operation(summary = "增加或修改Log")
    @PostMapping("saveOrUpdateLog")
    public void saveOrUpdateLog(@RequestBody FileLogDO fileLogDO) {
        fileLogService.saveOrUpdateLog(fileLogDO);
    }

    @Operation(summary = "删除Log")
    @PostMapping("deleteLog")
    public void deleteLog(@RequestParam Long id) {
        fileLogService.deleteById(id);
    }

    @Operation(summary = "分页查询IP")
    @GetMapping("listIpsPage")
    public PageVO<FileIpDO> listIpsPage(FileQuery fileQuery, PageQuery pageQuery) {
        return fileIpService.listIpsPage(fileQuery, pageQuery);
    }

}
