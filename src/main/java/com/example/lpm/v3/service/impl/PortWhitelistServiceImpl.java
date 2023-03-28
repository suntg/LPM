package com.example.lpm.v3.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.domain.entity.PortWhitelistDO;
import com.example.lpm.v3.mapper.PortWhitelistMapper;
import com.example.lpm.v3.service.PortWhitelistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class PortWhitelistServiceImpl extends ServiceImpl<PortWhitelistMapper, PortWhitelistDO> implements PortWhitelistService {
}
