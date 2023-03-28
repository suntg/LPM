package com.example.lpm.v3.service;

import org.springframework.web.bind.annotation.RequestParam;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.domain.entity.FileLogDO;
import com.example.lpm.v3.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.vo.PageVO;

public interface FileLogService extends IService<FileLogDO> {

    PageVO<FileLogDO> listLogsPage(@RequestParam FileQuery fileQuery, PageQuery pageQuery);

    FileLogDO getById(Long id);

    void saveOrUpdateLog(FileLogDO fileLogDO);

    void deleteById(Long id);
}
