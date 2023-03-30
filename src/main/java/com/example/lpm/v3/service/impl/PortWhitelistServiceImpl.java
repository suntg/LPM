package com.example.lpm.v3.service.impl;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.PortWhitelistDO;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.PortWhitelistQuery;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.mapper.PortWhitelistMapper;
import com.example.lpm.v3.service.PortWhitelistService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortWhitelistServiceImpl extends ServiceImpl<PortWhitelistMapper, PortWhitelistDO> implements PortWhitelistService {


    @Resource
    private PortWhitelistMapper portWhitelistMapper;


    @Override
    public int deleteById(Long id) {
        return portWhitelistMapper.deleteById(id);
    }

    @Override
    public int insert(PortWhitelistDO record) {
        long count = portWhitelistMapper.selectCount(new QueryWrapper<PortWhitelistDO>().lambda().eq(PortWhitelistDO::getPort, record.getPort()));
        if (count == 0L) {
            return portWhitelistMapper.insert(record);
        }
        return (int) count;
    }

    @Override
    public int update(PortWhitelistDO entity) {
        return portWhitelistMapper.updateById(entity);
    }

    @Override
    public PageVO<PortWhitelistDO> listPage(PortWhitelistQuery operationQuery, PageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());

        List<PortWhitelistDO> portWhitelistDOList = portWhitelistMapper.selectList(new QueryWrapper<PortWhitelistDO>().lambda()
                .eq(ObjectUtil.isNotEmpty(operationQuery.getPort()), PortWhitelistDO::getPort, operationQuery.getPort())
                .orderByDesc(PortWhitelistDO::getCreateTime)
        );
        return new PageVO<>(page.getTotal(), portWhitelistDOList);
    }
}
