package com.example.lpm.v3.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.FileIpDO;
import com.example.lpm.v3.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.mapper.FileIpMapper;
import com.example.lpm.v3.service.FileIpService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * AllArgsConstructor和RequiredArgsConstructor区别 都是通过自动生成构造函数注入bean， 区别就在于@AllArgsConstructor会将类中所有的属性都生成构造函数，
 * 而@RequiredArgsConstructor只会生成final修饰的字段。
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FileIpServiceImpl extends ServiceImpl<FileIpMapper, FileIpDO> implements FileIpService {

    private final FileIpMapper fileIpMapper;

    private ObjectMapper objectMapper;

    @Override
    public PageVO<FileIpDO> listIpsPage(FileQuery fileQuery, PageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<FileIpDO> fileIpDOList =
            fileIpMapper.selectList(new QueryWrapper<FileIpDO>().lambda().eq(FileIpDO::getFileId, fileQuery.getFileId())
                .eq(CharSequenceUtil.isNotBlank(fileQuery.getXLuminatiIp()), FileIpDO::getXLuminatiIp,
                    fileQuery.getXLuminatiIp())
                .likeRight(CharSequenceUtil.isNotBlank(fileQuery.getIp()), FileIpDO::getIp, fileQuery.getIp())
                .ge(ObjectUtil.isNotNull(fileQuery.getStartCreateTime()), FileIpDO::getCreateTime,
                    fileQuery.getStartCreateTime())
                .le(ObjectUtil.isNotNull(fileQuery.getEndCreateTime()), FileIpDO::getCreateTime,
                    fileQuery.getEndCreateTime())
                .orderByDesc(FileIpDO::getCreateTime));
        return new PageVO<>(page.getTotal(), fileIpDOList);
    }
}
