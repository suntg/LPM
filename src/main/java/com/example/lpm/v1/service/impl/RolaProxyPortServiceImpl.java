package com.example.lpm.v1.service.impl;

import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.constant.ProxyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v1.common.BizException;
import com.example.lpm.v1.common.ReturnCode;
import com.example.lpm.v1.config.GzipRequestInterceptor;
import com.example.lpm.v1.domain.entity.RolaIpDO;
import com.example.lpm.v1.domain.entity.RolaProxyPortDO;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.query.RolaQuery;
import com.example.lpm.v1.domain.request.RolaIpRequest;
import com.example.lpm.v1.domain.request.RolaStartSocksPortRequest;
import com.example.lpm.v1.domain.vo.PageVO;
import com.example.lpm.v1.mapper.RolaIpMapper;
import com.example.lpm.v1.mapper.RolaProxyPortMapper;
import com.example.lpm.v1.service.RolaProxyPortService;
import com.example.lpm.v1.util.ExecuteCommandUtil;
import com.example.lpm.v1.util.RolaUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;

@Service
@Slf4j
@RequiredArgsConstructor
public class RolaProxyPortServiceImpl extends ServiceImpl<RolaProxyPortMapper, RolaProxyPortDO>
    implements RolaProxyPortService {

    private final ObjectMapper objectMapper;

    private final RolaProxyPortMapper rolaProxyPortMapper;

    private final RolaIpMapper rolaIpMapper;

    @Override
    public PageVO<RolaProxyPortDO> listPortsPage(RolaQuery rolaQuery, PageQuery pageQuery) {
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<RolaProxyPortDO> rolaProxyPortDOList = rolaProxyPortMapper.listPorts(rolaQuery.getProxyPort());

        if (CollUtil.isNotEmpty(rolaProxyPortDOList)) {
            for (RolaProxyPortDO rolaProxyPortDO : rolaProxyPortDOList) {
                if (CharSequenceUtil.isNotBlank(rolaProxyPortDO.getCity())) {
                    String city = "";
                    for (String s : CharSequenceUtil.split(rolaProxyPortDO.getCity(), " ")) {
                        city = city + CharSequenceUtil.upperFirst(s) + " ";
                    }
                    rolaProxyPortDO.setCity(CharSequenceUtil.trim(city));
                }
                if (CharSequenceUtil.isNotBlank(rolaProxyPortDO.getRegion())) {
                    rolaProxyPortDO.setRegion(rolaProxyPortDO.getRegion().toUpperCase());
                }
                if (CharSequenceUtil.isNotBlank(rolaProxyPortDO.getCountry())) {
                    rolaProxyPortDO.setCountry(rolaProxyPortDO.getCountry().toUpperCase());
                }
            }
        }
        return new PageVO<>(page.getTotal(), rolaProxyPortDOList);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void deleteProxyPort(Long id) {
        RolaProxyPortDO rolaProxyPortDO = rolaProxyPortMapper.selectById(id);
        if (rolaProxyPortDO != null) {
            rolaProxyPortMapper.deleteByPrimaryKey(id);
            // 端口
            ExecuteCommandUtil.killRolaProxyByPort(String.valueOf(rolaProxyPortDO.getProxyPort()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProxyPortByPort(Integer port) {
        RolaProxyPortDO rolaProxyPortDO = rolaProxyPortMapper
            .selectOne(new QueryWrapper<RolaProxyPortDO>().lambda().eq(RolaProxyPortDO::getProxyPort, port));
        if (rolaProxyPortDO != null) {
            rolaProxyPortMapper.deleteByPrimaryKey(rolaProxyPortDO.getId());
            // 端口
            ExecuteCommandUtil.killRolaProxyByPort(String.valueOf(rolaProxyPortDO.getProxyPort()));
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchSocksPorts(List<Integer> ports) {
        for (Integer port : ports) {
            deleteProxyPortByPort(port);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteAllProxyPort() {
        String result = RuntimeUtil.execForStr("killall -9 proxy");
        log.info("kill all proxy:{}", result);

        List<RolaProxyPortDO> rolaProxyPortDOList = rolaProxyPortMapper.selectList(new QueryWrapper<>());

        List idNoList = rolaProxyPortDOList.stream().map(RolaProxyPortDO::getId).collect(Collectors.toList());

        rolaProxyPortMapper.deleteBatchIds(idNoList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProxyPortByIp(String ip) {
        RolaProxyPortDO rolaProxyPortDO = rolaProxyPortMapper
            .selectOne(new QueryWrapper<RolaProxyPortDO>().lambda().eq(RolaProxyPortDO::getRolaIp, ip));
        if (rolaProxyPortDO != null) {
            rolaProxyPortMapper.deleteByPrimaryKey(rolaProxyPortDO.getId());
            // 端口
            ExecuteCommandUtil.killProcessByPort(rolaProxyPortDO.getProxyPort());
        }

    }

    @Override
    public void startProxyPort(RolaIpRequest rolaIpRequest) throws Exception {
        if (rolaIpRequest.getProxyPort() == null) {
            throw new BizException(ReturnCode.RC999.getCode(), "端口不为空");
        }

        RolaProxyPortDO rolaProxyPortDO = rolaProxyPortMapper.selectOne(new QueryWrapper<RolaProxyPortDO>().lambda()
            .eq(RolaProxyPortDO::getProxyPort, rolaIpRequest.getProxyPort()));

        if (rolaProxyPortDO != null) {
            throw new BizException(997, "端口在使用中");
        }
        if (StrUtil.isBlank(rolaIpRequest.getCountry())) {
            rolaIpRequest.setCountry("us");
        }
        if (StrUtil.isNotBlank(rolaIpRequest.getCountry())) {
            rolaIpRequest.setCountry(rolaIpRequest.getCountry().toLowerCase());
        }
        if (StrUtil.isNotBlank(rolaIpRequest.getState())) {
            rolaIpRequest.setState(rolaIpRequest.getState().toLowerCase());
        }
        if (StrUtil.isNotBlank(rolaIpRequest.getCity())) {
            rolaIpRequest.setCity(rolaIpRequest.getCity().toLowerCase());
        }

        long count = rolaIpMapper.selectCount(new QueryWrapper<RolaIpDO>().lambda()
            .eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
            .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity, rolaIpRequest.getCity())
            .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion, rolaIpRequest.getState()));

        if (count > 0) {
            int c = RandomUtil.randomInt(0, (int)count);
            RolaIpDO rolaIpDO = rolaIpMapper.selectOne(new QueryWrapper<RolaIpDO>().lambda()
                .eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
                .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity, rolaIpRequest.getCity())
                .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion,
                    rolaIpRequest.getState())
                .last("limit " + c + " , 1"));

            int userNum = RandomUtil.randomInt(10, 100000);

            String spsResult =
                ExecuteCommandUtil.executeRolaProxySps(rolaIpRequest.getProxyPort(), userNum, rolaIpDO.getIp());

            Thread.sleep(10000);
            try {
                if (ExecuteCommandUtil.rolaProxyState(spsResult)) {
                    String localSpsResult = ExecuteCommandUtil.rolaLocalLumtest(rolaIpRequest.getProxyPort());

                    LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(localSpsResult, LuminatiIPDTO.class);

                    if (!StrUtil.equals(rolaIpDO.getIp(), luminatiIPDTO.getIp())) {
                        throw new BizException(ReturnCode.RC999.getCode(), "IP不同，测活失败");
                    }
                    if (!CharSequenceUtil.equals(rolaIpDO.getPostalCode(), luminatiIPDTO.getGeo().getPostalCode())) {
                        throw new BizException(ReturnCode.RC999.getCode(), "PostalCode不同，测活失败");
                    }

                    RolaProxyPortDO rolaProxyPort = new RolaProxyPortDO();
                    rolaProxyPort.setProxyPort(rolaIpRequest.getProxyPort());
                    rolaProxyPort.setRolaIp(rolaIpDO.getIp());
                    rolaProxyPort.setName(rolaIpRequest.getName());
                    rolaProxyPort.setCountry(rolaIpDO.getCountry());
                    rolaProxyPort.setCity(rolaIpDO.getCity());
                    rolaProxyPort.setRegion(rolaIpDO.getRegion());
                    rolaProxyPort.setExpirationTime(LocalDateTime.now());
                    rolaProxyPortMapper.insert(rolaProxyPort);
                } else {
                    throw new BizException(ReturnCode.RC999.getCode(), "sps 启动失败");
                }
            } catch (Exception e) {
                ExecuteCommandUtil.killProcessByPort(rolaIpRequest.getProxyPort());
                throw e;
            }
        } else {
            for (int i = 0; i < 10; i++) {
                int userNum = RandomUtil.randomInt(10, 100000);
                String user = "skyescn_" + userNum;
                String result = ExecuteCommandUtil.rolaRefresh(user, rolaIpRequest.getCountry(),
                    rolaIpRequest.getState(), rolaIpRequest.getCity());
                if (StrUtil.contains(result, "SUCCESS")) {

                    String lumtest = ExecuteCommandUtil.rolaLumtest(user);

                    log.info("lumtest :{}", lumtest);

                    try {
                        LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(lumtest, LuminatiIPDTO.class);

                        saveOrUpdate(luminatiIPDTO);
                    } catch (Exception e) {
                        log.error("没有符合的IP:{}", ExceptionUtil.stacktraceToString(e));
                    }
                }
            }
            count = rolaIpMapper
                .selectCount(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
                    .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity,
                        rolaIpRequest.getCity())
                    .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion,
                        rolaIpRequest.getState()));

            if (count > 0) {
                int c = RandomUtil.randomInt(0, (int)count);
                RolaIpDO rolaIpDO = rolaIpMapper.selectOne(
                    new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity,
                            rolaIpRequest.getCity())
                        .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion,
                            rolaIpRequest.getState())
                        .last("limit " + c + " , 1"));

                int userNum = RandomUtil.randomInt(10, 100000);

                String spsResult =
                    ExecuteCommandUtil.executeRolaProxySps(rolaIpRequest.getProxyPort(), userNum, rolaIpDO.getIp());
                Thread.sleep(10000);
                try {
                    if (ExecuteCommandUtil.rolaProxyState(spsResult)) {
                        String localSpsResult = ExecuteCommandUtil.rolaLocalLumtest(rolaIpRequest.getProxyPort());

                        LuminatiIPDTO luminatiIPDTO = null;
                        try {
                            luminatiIPDTO = objectMapper.readValue(localSpsResult, LuminatiIPDTO.class);
                        } catch (JsonProcessingException e) {
                            log.error("测活失败");
                            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest失败");
                        }

                        if (!StrUtil.equals(rolaIpDO.getIp(), luminatiIPDTO.getIp())) {
                            throw new BizException(ReturnCode.RC999.getCode(), "IP不同，测活失败");
                        }
                        if (!StrUtil.equals(rolaIpDO.getPostalCode(), luminatiIPDTO.getGeo().getPostalCode())) {
                            throw new BizException(ReturnCode.RC999.getCode(), "PostalCode不同，测活失败");
                        }

                        RolaProxyPortDO rolaProxyPort = new RolaProxyPortDO();
                        rolaProxyPort.setProxyPort(rolaIpRequest.getProxyPort());
                        rolaProxyPort.setRolaIp(rolaIpDO.getIp());
                        rolaProxyPort.setName(rolaIpRequest.getName());
                        rolaProxyPort.setCountry(rolaIpDO.getCountry());
                        rolaProxyPort.setCity(rolaIpDO.getCity());
                        rolaProxyPort.setRegion(rolaIpDO.getRegion());
                        rolaProxyPort.setExpirationTime(LocalDateTime.now());
                        rolaProxyPortMapper.insert(rolaProxyPort);
                    } else {
                        throw new BizException(ReturnCode.RC999.getCode(), "sps 启动失败");
                    }
                } catch (Exception e) {
                    ExecuteCommandUtil.killProcessByPort(rolaIpRequest.getProxyPort());
                    throw e;
                }
            } else {
                throw new BizException(ReturnCode.RC999.getCode(), "连续获取十次后失败");
            }

        }
    }

    @Override
    public void changeProxyIp(RolaIpRequest rolaIpRequest) throws Exception {
        log.info("rolaIpRequest:{}", objectMapper.writeValueAsString(rolaIpRequest));
        RolaProxyPortDO rolaProxyPortDO = rolaProxyPortMapper.selectOne(new QueryWrapper<RolaProxyPortDO>().lambda()
            .eq(RolaProxyPortDO::getProxyPort, rolaIpRequest.getProxyPort()));
        if (StrUtil.isBlank(rolaIpRequest.getCountry())) {
            rolaIpRequest.setCountry("us");
        }
        if (StrUtil.isNotBlank(rolaIpRequest.getCountry())) {
            rolaIpRequest.setCountry(rolaIpRequest.getCountry().toLowerCase());
        }
        if (StrUtil.isNotBlank(rolaIpRequest.getState())) {
            rolaIpRequest.setState(rolaIpRequest.getState().toLowerCase());
        }
        if (StrUtil.isNotBlank(rolaIpRequest.getCity())) {
            rolaIpRequest.setCity(rolaIpRequest.getCity().toLowerCase());
        }
        long count = rolaIpMapper.selectCount(new QueryWrapper<RolaIpDO>().lambda()
            .eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
            .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity, rolaIpRequest.getCity())
            .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion, rolaIpRequest.getState())
            .ne(RolaIpDO::getIp, rolaProxyPortDO.getRolaIp()));

        if (count > 0) {
            int c = RandomUtil.randomInt(0, (int)count);
            RolaIpDO rolaIpDO = rolaIpMapper.selectOne(new QueryWrapper<RolaIpDO>().lambda()
                .eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
                .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity, rolaIpRequest.getCity())
                .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion,
                    rolaIpRequest.getState())
                .ne(RolaIpDO::getIp, rolaProxyPortDO.getRolaIp()).last("limit " + c + " , 1"));

            int userNum = RandomUtil.randomInt(10, 100000);

            String spsResult =
                ExecuteCommandUtil.executeRolaProxySps(rolaIpRequest.getProxyPort(), userNum, rolaIpDO.getIp());
            Thread.sleep(10000);
            try {
                if (ExecuteCommandUtil.rolaProxyState(spsResult)) {
                    String localSpsResult = ExecuteCommandUtil.rolaLocalLumtest(rolaIpRequest.getProxyPort());

                    LuminatiIPDTO luminatiIPDTO = null;
                    try {
                        luminatiIPDTO = objectMapper.readValue(localSpsResult, LuminatiIPDTO.class);
                    } catch (JsonProcessingException e) {
                        log.error("测活失败");
                        throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest失败");
                    }

                    if (!StrUtil.equals(rolaIpDO.getIp(), luminatiIPDTO.getIp())) {
                        throw new BizException(ReturnCode.RC999.getCode(), "IP不同，测活失败");
                    }
                    if (!StrUtil.equals(rolaIpDO.getPostalCode(), luminatiIPDTO.getGeo().getPostalCode())) {
                        throw new BizException(ReturnCode.RC999.getCode(), "PostalCode不同，测活失败");
                    }

                    // rolaProxyPortDO.setProxyPort(rolaIpRequest.getProxyPort());
                    rolaProxyPortDO.setRolaIp(rolaIpDO.getIp());
                    rolaProxyPortDO.setCountry(rolaIpDO.getCountry());
                    rolaProxyPortDO.setCity(rolaIpDO.getCity());
                    rolaProxyPortDO.setRegion(rolaIpDO.getRegion());
                    rolaProxyPortDO.setExpirationTime(LocalDateTime.now());

                    rolaProxyPortMapper.updateById(rolaProxyPortDO);
                } else {
                    throw new BizException(ReturnCode.RC999.getCode(), "sps 启动失败");
                }
            } catch (Exception e) {
                ExecuteCommandUtil.killProcessByPort(rolaIpRequest.getProxyPort());
                throw e;
            }
        } else {
            for (int i = 0; i < 10; i++) {
                int userNum = RandomUtil.randomInt(10, 100000);
                String user = "skyescn_" + userNum;
                String result = ExecuteCommandUtil.rolaRefresh(user, rolaIpRequest.getCountry(),
                    rolaIpRequest.getState(), rolaIpRequest.getCity());
                if (StrUtil.contains(result, "SUCCESS")) {

                    String lumtest = ExecuteCommandUtil.rolaLumtest(user);

                    log.info("lumtest :{}", lumtest);

                    try {
                        LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(lumtest, LuminatiIPDTO.class);

                        saveOrUpdate(luminatiIPDTO);
                    } catch (Exception e) {
                        log.error("没有符合的IP:{}", ExceptionUtil.stacktraceToString(e));
                    }
                }
            }
            count = rolaIpMapper
                .selectCount(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
                    .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity,
                        rolaIpRequest.getCity())
                    .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion,
                        rolaIpRequest.getState()));

            if (count > 0) {
                int c = RandomUtil.randomInt(0, (int)count);
                RolaIpDO rolaIpDO = rolaIpMapper.selectOne(
                    new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getCountry, rolaIpRequest.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getCity()), RolaIpDO::getCity,
                            rolaIpRequest.getCity())
                        .eq(CharSequenceUtil.isNotBlank(rolaIpRequest.getState()), RolaIpDO::getRegion,
                            rolaIpRequest.getState())
                        .last("limit " + c + " , 1"));

                int userNum = RandomUtil.randomInt(10, 100000);

                String spsResult =
                    ExecuteCommandUtil.executeRolaProxySps(rolaIpRequest.getProxyPort(), userNum, rolaIpDO.getIp());
                Thread.sleep(10000);
                try {
                    if (ExecuteCommandUtil.rolaProxyState(spsResult)) {
                        String localSpsResult = ExecuteCommandUtil.rolaLocalLumtest(rolaIpRequest.getProxyPort());

                        LuminatiIPDTO luminatiIPDTO = null;
                        try {
                            luminatiIPDTO = objectMapper.readValue(localSpsResult, LuminatiIPDTO.class);
                        } catch (JsonProcessingException e) {
                            log.error("测活失败");
                            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest失败");
                        }

                        if (!StrUtil.equals(rolaIpDO.getIp(), luminatiIPDTO.getIp())) {
                            throw new BizException(ReturnCode.RC999.getCode(), "IP不同，测活失败");
                        }
                        if (!StrUtil.equals(rolaIpDO.getPostalCode(), luminatiIPDTO.getGeo().getPostalCode())) {
                            throw new BizException(ReturnCode.RC999.getCode(), "PostalCode不同，测活失败");
                        }

                        RolaProxyPortDO rolaProxyPort = new RolaProxyPortDO();
                        rolaProxyPort.setProxyPort(rolaIpRequest.getProxyPort());
                        rolaProxyPort.setRolaIp(rolaIpDO.getIp());
                        rolaProxyPort.setName(rolaIpRequest.getName());
                        rolaProxyPort.setCountry(rolaIpDO.getCountry());
                        rolaProxyPort.setCity(rolaIpDO.getCity());
                        rolaProxyPort.setRegion(rolaIpDO.getRegion());
                        rolaProxyPort.setExpirationTime(LocalDateTime.now());
                        rolaProxyPortMapper.insert(rolaProxyPort);
                    } else {
                        throw new BizException(ReturnCode.RC999.getCode(), "sps 启动失败");
                    }
                } catch (Exception e) {
                    ExecuteCommandUtil.killProcessByPort(rolaIpRequest.getProxyPort());
                    throw e;
                }
            } else {
                throw new BizException(ReturnCode.RC999.getCode(), "连续获取十次后失败");
            }

        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean startSocksPort(RolaStartSocksPortRequest startSocksPortRequest) {
        RolaProxyPortDO rolaProxyPortDO = rolaProxyPortMapper.selectOne(new QueryWrapper<RolaProxyPortDO>().lambda()
            .eq(RolaProxyPortDO::getProxyPort, startSocksPortRequest.getSocksPort()));

        if (rolaProxyPortDO != null) {
            throw new BizException(997, "端口在使用中");
        }

        RolaIpDO rolaIpDO = rolaIpMapper.selectOne(
            new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, startSocksPortRequest.getSocksAddressIp()));
        if (rolaIpDO == null) {
            throw new BizException(ReturnCode.RC999.getCode(), "IP不存在");
        }

        if (CharSequenceUtil.hasBlank(startSocksPortRequest.getSocksUsername(),
            startSocksPortRequest.getSocksPassword(), startSocksPortRequest.getRolaPassword())) {
            throw new BizException(ReturnCode.RC999.getCode(), "参数不能为空");
        }

        if (CharSequenceUtil.isBlank(startSocksPortRequest.getRolaUsername())) {
            String user = RolaUtil.randomUsername();
            startSocksPortRequest.setRolaUsername(user);
        }

        String rolaUsername =
            startSocksPortRequest.getRolaUsername() + "-ip-" + startSocksPortRequest.getSocksAddressIp();
        try {
            Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("gate2.rola.info", 2042));
            java.net.Authenticator.setDefault(new java.net.Authenticator() {

                private final PasswordAuthentication authentication =
                    new PasswordAuthentication(rolaUsername, startSocksPortRequest.getRolaPassword().toCharArray());

                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return authentication;
                }
            });

            OkHttpClient client =
                new OkHttpClient().newBuilder().proxy(proxy).addInterceptor(new GzipRequestInterceptor()).build();
            Request request = new Request.Builder().url(ProxyConstant.LUMTEST_URL).build();

            okhttp3.Response response = client.newCall(request).execute();
            String responseString = response.body().string();
            LuminatiIPDTO luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);

            if (!CharSequenceUtil.equals(rolaIpDO.getIp(), luminatiIPDTO.getIp())) {
                rolaIpDO.setStatus(0);
                rolaIpMapper.updateById(rolaIpDO);
                throw new BizException(998, "IP不相等");
            }
        } catch (Exception e) {
            rolaIpDO.setStatus(0);
            rolaIpMapper.updateById(rolaIpDO);
            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest返回失败");
        }

        ExecuteCommandUtil.executeRolaProxySps(startSocksPortRequest.getSocksPort(),
            startSocksPortRequest.getSocksUsername(), startSocksPortRequest.getSocksPassword(),
            startSocksPortRequest.getSocksAddressIp(), startSocksPortRequest.getRolaUsername(),
            startSocksPortRequest.getRolaPassword());

        rolaIpDO.setLastUseTime(LocalDateTime.now());
        if (rolaIpDO.getUseNumber() == null) {
            rolaIpDO.setUseNumber(0);
        }
        rolaIpDO.setUseNumber(rolaIpDO.getUseNumber() + 1);
        rolaIpMapper.updateById(rolaIpDO);

        RolaProxyPortDO rolaProxyPort = new RolaProxyPortDO();
        rolaProxyPort.setProxyPort(startSocksPortRequest.getSocksPort());
        rolaProxyPort.setRolaIp(startSocksPortRequest.getSocksAddressIp());
        rolaProxyPort.setName(startSocksPortRequest.getDeviceName());
        rolaProxyPortMapper.insert(rolaProxyPort);
        return true;
    }

    private void saveOrUpdate(LuminatiIPDTO luminatiIPDTO) {

        long count =
            rolaIpMapper.selectCount(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, luminatiIPDTO.getIp()));
        if (count > 0) {
            log.info("已存在IP: {}", luminatiIPDTO.getIp());
            rolaIpMapper.update(new RolaIpDO(),
                new UpdateWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, luminatiIPDTO.getIp())
                    .set(RolaIpDO::getCountry, luminatiIPDTO.getCountry().toLowerCase())
                    .set(RolaIpDO::getRegion, luminatiIPDTO.getGeo().getRegion().toLowerCase())
                    .set(RolaIpDO::getCity, luminatiIPDTO.getGeo().getCity().toLowerCase())
                    .set(RolaIpDO::getTz, luminatiIPDTO.getGeo().getTz()));
        } else {
            RolaIpDO rolaIpDO = new RolaIpDO();
            rolaIpDO.setIp(luminatiIPDTO.getIp());
            rolaIpDO.setCountry(luminatiIPDTO.getCountry().toLowerCase());
            rolaIpDO.setRegion(luminatiIPDTO.getGeo().getRegion().toLowerCase());
            rolaIpDO.setCity(luminatiIPDTO.getGeo().getCity().toLowerCase());
            rolaIpDO.setPostalCode(luminatiIPDTO.getGeo().getPostalCode());
            rolaIpDO.setTz(luminatiIPDTO.getGeo().getTz());

            rolaIpMapper.insert(rolaIpDO);
            log.info("插入新数据: {}", rolaIpDO);
        }

    }

}
