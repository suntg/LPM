/*
package com.example.lpm.v3.controller;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.lpm.v3.domain.entity.FileIpDO;
import com.example.lpm.v3.domain.entity.FileLogDO;
import com.example.lpm.v3.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.vo.FileVO;
import com.example.lpm.v3.service.FileIpService;
import com.example.lpm.v3.service.FileLogService;
import com.example.lpm.v3.service.FileService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "File")
@Slf4j
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    private final FileIpService fileIpService;

    private final FileLogService fileLogService;

    @Operation(summary = "分页查询File")
    @GetMapping("listFilesPage")
    public Page<FileVO> listFilesByPage(FileQuery fileQuery, PageQuery pageQuery) {
        return fileService.listFilesByPage(fileQuery, pageQuery);
    }

    @Operation(summary = "查询多个File详情")
    @GetMapping("listFiles")
    public List<FileVO> listFiles(@RequestParam(value = "fileIds") List<Long> fileIds) {
        return fileService.listFiles(fileIds);
    }

    @Operation(summary = "分页查询Log")
    @GetMapping("listLogsPage")
    public Page<FileLogDO> listFileLogsByPage(FileQuery fileQuery, PageQuery pageQuery) {
        return fileLogService.listFileLogsByPage(fileQuery, pageQuery);
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
    public Page<FileIpDO> listIpsPage(FileQuery fileQuery, PageQuery pageQuery) {
        return fileIpService.listFileIpsByPage(fileQuery, pageQuery);
    }

}
*/
