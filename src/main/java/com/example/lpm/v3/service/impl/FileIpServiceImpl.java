/*
package com.example.lpm.v3.service.impl;

import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.FileIpDO;
import com.example.lpm.v3.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.mapper.FileIpMapper;
import com.example.lpm.v3.service.FileIpService;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

*/
/**
 * AllArgsConstructor和RequiredArgsConstructor区别 都是通过自动生成构造函数注入bean， 区别就在于@AllArgsConstructor会将类中所有的属性都生成构造函数，
 * 而@RequiredArgsConstructor只会生成final修饰的字段。
 *//*

@Service
@Slf4j
@RequiredArgsConstructor
public class FileIpServiceImpl extends ServiceImpl<FileIpMapper, FileIpDO> implements FileIpService {

    private final FileIpMapper fileIpMapper;

    @Override
    public Page<FileIpDO> listFileIpsByPage(FileQuery fileQuery, PageQuery pageQuery) {

        return this.page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()),
            new QueryWrapper<FileIpDO>().lambda().eq(FileIpDO::getFileId, fileQuery.getFileId())
                .eq(CharSequenceUtil.isNotBlank(fileQuery.getXLuminatiIp()), FileIpDO::getXLuminatiIp,
                    fileQuery.getXLuminatiIp())
                .likeRight(CharSequenceUtil.isNotBlank(fileQuery.getIp()), FileIpDO::getIp, fileQuery.getIp())
                .ge(ObjectUtil.isNotNull(fileQuery.getStartCreateTime()), FileIpDO::getCreateTime,
                    fileQuery.getStartCreateTime())
                .le(ObjectUtil.isNotNull(fileQuery.getEndCreateTime()), FileIpDO::getCreateTime,
                    fileQuery.getEndCreateTime())
                .orderByDesc(FileIpDO::getCreateTime));

    }
}
*/
