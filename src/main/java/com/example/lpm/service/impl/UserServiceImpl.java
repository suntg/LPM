package com.example.lpm.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.domain.entity.UserDO;
import com.example.lpm.mapper.UserMapper;
import com.example.lpm.service.UserService;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.domain.entity.OperationLogDO;
import com.example.lpm.v3.service.OperationLogService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;

@Slf4j
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

            // 登陆操作记录到操作日志表中
            OperationLogDO operationLogDO = new OperationLogDO();
            operationLogDO.setRequestUri("登录");
            operationLogDO.setIp(ip);

            try {
                String result = HttpUtil.get("https://ip.useragentinfo.com/json?ip=" + operationLogDO.getIp());
                JSONObject jsonObject = JSON.parseObject(result);
                operationLogDO.setCountry(jsonObject.getString("country"));
                operationLogDO.setRegion(jsonObject.getString("province"));
                operationLogDO.setCity(jsonObject.getString("city"));
            } catch (Exception e) {
                log.error("ip.useragentinfo.com 查询{}异常:{}", operationLogDO.getIp(), ExceptionUtil.stacktraceToString(e));
            }

            operationLogDO.setCreateTime(LocalDateTime.now());
            operationLogService.save(operationLogDO);
            return saTokenInfo.getTokenValue();
        } else {
            throw new BizException(10001, "用户名或密码错误");
        }
    }

}
