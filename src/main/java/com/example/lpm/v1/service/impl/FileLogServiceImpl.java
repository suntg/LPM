package com.example.lpm.v1.service.impl;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v1.domain.entity.FileLogDO;
import com.example.lpm.v1.domain.query.FileQuery;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.vo.PageVO;
import com.example.lpm.v1.mapper.FileLogMapper;
import com.example.lpm.v1.service.FileLogService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileLogServiceImpl extends ServiceImpl<FileLogMapper, FileLogDO> implements FileLogService {

    @Resource
    private FileLogMapper fileLogMapper;

    @Override
    public PageVO<FileLogDO> listLogsPage(FileQuery fileQuery, PageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<FileLogDO> fileLogDOList = fileLogMapper
            .selectList(new QueryWrapper<FileLogDO>().lambda().eq(FileLogDO::getFileId, fileQuery.getFileId())
                .like(CharSequenceUtil.isNotBlank(fileQuery.getLogContent()), FileLogDO::getContent,
                    fileQuery.getLogContent())
                .eq(ObjectUtil.isNotNull(fileQuery.getLogType()), FileLogDO::getType, fileQuery.getLogType())
                .ge(ObjectUtil.isNotNull(fileQuery.getStartCreateTime()), FileLogDO::getCreateTime,
                    fileQuery.getStartCreateTime())
                .le(ObjectUtil.isNotNull(fileQuery.getEndCreateTime()), FileLogDO::getCreateTime,
                    fileQuery.getEndCreateTime())
                .orderByDesc(FileLogDO::getCreateTime));
        return new PageVO<>(page.getTotal(), fileLogDOList);
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
