package com.example.lpm.service.impl;

import javax.annotation.Resource;

import com.example.lpm.util.IpUtil;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import com.example.lpm.v3.service.OperationLogService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.domain.entity.UserDO;
import com.example.lpm.mapper.UserMapper;
import com.example.lpm.service.UserService;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserDO> implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private OperationLogService operationLogService;

    @Override
    public String login(String username, String password, String ip) {
        UserDO userDO = userMapper.selectOne(new QueryWrapper<UserDO>().lambda().eq(UserDO::getUsername, username)
            .eq(UserDO::getUserType, 1).eq(UserDO::getState, 1));
        if (userDO == null) {
            throw new BizException(10001, "用户名或密码错误");
        }
        if (Integer.valueOf(2).equals(userDO.getUserType())
            && (StringUtils.isBlank(ip) || StringUtils.equals(ip, userDO.getIp()))) {
            throw new BizException(10001, "用户名或密码错误");
        }
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.encode("123456"));
        // 校验密码
        boolean matchResult = passwordEncoder.matches(password, userDO.getPassword());
        if (matchResult) {
             StpUtil.login(userDO.getId());
            SaTokenInfo saTokenInfo = StpUtil.getTokenInfo();

            return saTokenInfo.getTokenValue();
        } else {
            throw new BizException(10001, "用户名或密码错误");
        }
    }

}
