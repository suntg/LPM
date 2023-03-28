package com.example.lpm.v3.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.domain.entity.ProxyPortDO;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.ProxyPortQuery;
import com.example.lpm.v3.domain.request.DeleteProxyPortRequest;
import com.example.lpm.v3.mapper.ProxyPortMapper;
import com.example.lpm.v3.service.ProxyPortService;
import com.example.lpm.v3.util.ExecuteCommandUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.RuntimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
@Deprecated
@Service
@Slf4j
@RequiredArgsConstructor
public class ProxyPortServiceImpl extends ServiceImpl<ProxyPortMapper, ProxyPortDO> implements ProxyPortService {

    private final ProxyPortMapper proxyPortMapper;

    @Override
    public PageVO<ProxyPortDO> listProxyPortsByPage(ProxyPortQuery proxyPortQuery, PageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<ProxyPortDO> proxyPortDOList = proxyPortMapper.listProxyPorts(proxyPortQuery);
        if (CollUtil.isNotEmpty(proxyPortDOList)) {
            for (ProxyPortDO proxyPortDO : proxyPortDOList) {
                if (CharSequenceUtil.isNotBlank(proxyPortDO.getCity())) {
                    String city = "";
                    for (String s : CharSequenceUtil.split(proxyPortDO.getCity(), " ")) {
                        city = city + CharSequenceUtil.upperFirst(s) + " ";
                    }
                    proxyPortDO.setCity(CharSequenceUtil.trim(city));
                }
                if (CharSequenceUtil.isNotBlank(proxyPortDO.getRegion())) {
                    proxyPortDO.setRegion(proxyPortDO.getRegion().toUpperCase());
                }
                if (CharSequenceUtil.isNotBlank(proxyPortDO.getCountry())) {
                    proxyPortDO.setCountry(proxyPortDO.getCountry().toUpperCase());
                }
            }
        }
        return new PageVO<>(page.getTotal(), proxyPortDOList);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProxyPortByIp(DeleteProxyPortRequest deleteProxyPortRequest) {
        ProxyPortDO proxyPortDO = proxyPortMapper
            .selectOne(new QueryWrapper<ProxyPortDO>().lambda().eq(ProxyPortDO::getIp, deleteProxyPortRequest.getIp())
                .eq(ProxyPortDO::getTypeName, deleteProxyPortRequest.getTypeName()));
        if (ObjectUtil.isNotNull(proxyPortDO)) {
            proxyPortMapper.deleteById(proxyPortDO.getId());
            ExecuteCommandUtil.killProcessByPort(proxyPortDO.getProxyPort());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProxyPort(Long id) {
        ProxyPortDO proxyPortDO = proxyPortMapper.selectById(id);
        if (ObjectUtil.isNotNull(proxyPortDO)) {
            proxyPortMapper.deleteById(id);
            ExecuteCommandUtil.killProcessByPort(proxyPortDO.getProxyPort());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchProxyPorts(List<Long> ids) {
        for (Long id : ids) {
            deleteProxyPort(id);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllProxyPort() {
        String result = RuntimeUtil.execForStr("killall -9 proxy");
        log.info("kill all proxy:{}", result);

        List<ProxyPortDO> proxyPortDOList = proxyPortMapper.selectList(new QueryWrapper<>());
        if (CollectionUtil.isEmpty(proxyPortDOList)) {
            List idNoList = proxyPortDOList.stream().map(ProxyPortDO::getId).collect(Collectors.toList());
            proxyPortMapper.deleteBatchIds(idNoList);
        }

    }
}
