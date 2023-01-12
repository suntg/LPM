/*
package com.example.lpm.v3.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.FileLogDO;
import com.example.lpm.v3.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.mapper.FileLogMapper;
import com.example.lpm.v3.service.FileLogService;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FileLogServiceImpl extends ServiceImpl<FileLogMapper, FileLogDO> implements FileLogService {

    private final FileLogMapper fileLogMapper;

    @Override
    public Page<FileLogDO> listFileLogsByPage(FileQuery fileQuery, PageQuery pageQuery) {
        return this.page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()),
            new QueryWrapper<FileLogDO>().lambda().eq(FileLogDO::getFileId, fileQuery.getFileId())
                .like(CharSequenceUtil.isNotBlank(fileQuery.getLogContent()), FileLogDO::getContent,
                    fileQuery.getLogContent())
                .eq(ObjectUtil.isNotNull(fileQuery.getLogType()), FileLogDO::getType, fileQuery.getLogType())
                .ge(ObjectUtil.isNotNull(fileQuery.getStartCreateTime()), FileLogDO::getCreateTime,
                    fileQuery.getStartCreateTime())
                .le(ObjectUtil.isNotNull(fileQuery.getEndCreateTime()), FileLogDO::getCreateTime,
                    fileQuery.getEndCreateTime())
                .orderByDesc(FileLogDO::getCreateTime));
    }

    @Override
    public FileLogDO getById(Long id) {
        return fileLogMapper.selectById(id);
    }

    @Override
    public void saveOrUpdateLog(FileLogDO fileLogDO) {
        this.saveOrUpdate(fileLogDO);
    }

    @Override
    public void deleteById(Long id) {
        fileLogMapper.deleteById(id);
    }
}
*/
