package com.example.lpm.v1.service.impl;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v1.common.BizException;
import com.example.lpm.v1.common.ReturnCode;
import com.example.lpm.v1.constant.LumIPConstant;
import com.example.lpm.v1.constant.RedisLockKeyConstant;
import com.example.lpm.v1.domain.entity.LumIPDO;
import com.example.lpm.v1.domain.query.FindSocksPortQuery;
import com.example.lpm.v1.domain.query.ZipCodeQuery;
import com.example.lpm.v1.domain.request.LumIPActiveRequest;
import com.example.lpm.v1.domain.request.LumIPCollectRequest;
import com.example.lpm.v1.domain.request.RolaIpLockRequest;
import com.example.lpm.v1.mapper.LumIPMapper;
import com.example.lpm.v1.service.LumIPService;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class LumIPServiceImpl extends ServiceImpl<LumIPMapper, LumIPDO> implements LumIPService {

    private final LumIPMapper lumIPMapper;
    private final RedissonClient redissonClient;
    private final ObjectMapper objectMapper;

    @Override
    public List<LumIPDO> listByFileFlag(String fileFlag, String fileType) {
        return lumIPMapper.selectList(new QueryWrapper<LumIPDO>().lambda().like(LumIPDO::getFileFlag, fileFlag)
            .like(CharSequenceUtil.isNotBlank(fileType), LumIPDO::getFileType, fileType)
            .orderByDesc(LumIPDO::getUpdateTime));
    }

    @Override
    public void submitIpLock(RolaIpLockRequest rolaIpLockRequest) {
        lumIPMapper.update(new LumIPDO(),
            new UpdateWrapper<LumIPDO>().lambda().eq(LumIPDO::getIp, rolaIpLockRequest.getSocksAddressIp())
                .set(CharSequenceUtil.isNotBlank(rolaIpLockRequest.getFileFlag()), LumIPDO::getFileFlag,
                    rolaIpLockRequest.getFileFlag())
                .set(CharSequenceUtil.isNotBlank(rolaIpLockRequest.getFileType()), LumIPDO::getFileType,
                    rolaIpLockRequest.getFileType())
                .set(LumIPDO::getStatus, rolaIpLockRequest.getStatus()));
    }

    @Override
    public LumIPDO checkIpLock(RolaIpLockRequest rolaIpLockRequest) {
        LumIPDO lumIPDO = lumIPMapper
            .selectOne(new QueryWrapper<LumIPDO>().lambda().eq(LumIPDO::getIp, rolaIpLockRequest.getSocksAddressIp()));
        if (lumIPDO != null) {
            return lumIPDO;
        } else {
            throw new BizException(ReturnCode.RC999.getCode(), "数据不存在");
        }
    }

    @Override
    public LumIPDO findSocksPort(FindSocksPortQuery findSocksPortQuery) {
        // 加锁 lock("#ROLA_FIND_LOCK")
        RLock rLock = redissonClient.getLock(RedisLockKeyConstant.LUM_IP_FIND_LOCK);
        if (rLock.isLocked()) {
            log.error("LUM_IP_FIND_LOCK获取锁失败:{}", RedisLockKeyConstant.LUM_IP_FIND_LOCK);
            throw new BizException(ReturnCode.RC500.getCode(), "获取锁失败");
        }
        rLock.lock(5, TimeUnit.SECONDS);

        if (CharSequenceUtil.isBlank(findSocksPortQuery.getCountry())) {
            findSocksPortQuery.setCountry("us");
        }

        if (CharSequenceUtil.isNotBlank(findSocksPortQuery.getCountry())) {
            findSocksPortQuery.setCountry(findSocksPortQuery.getCountry().toLowerCase());
        }
        if (CharSequenceUtil.isNotBlank(findSocksPortQuery.getState())) {
            findSocksPortQuery.setState(findSocksPortQuery.getState().toLowerCase());
        }
        if (CharSequenceUtil.isNotBlank(findSocksPortQuery.getCity())) {
            findSocksPortQuery.setCity(findSocksPortQuery.getCity().toLowerCase());
        }

        if (CollUtil.isNotEmpty(findSocksPortQuery.getZipCodeList())) {

            // 先distance排序 小到大
            // "zip_code"= "78717",”78665“ + risk <= middle risk + status 1 list
            // list取第一个
            // 更新 rolaIpDO.setStatus(4);
            // 返回
            List<ZipCodeQuery> zipCodeQueryList = findSocksPortQuery.getZipCodeList();
            List<String> zipCodeList =
                zipCodeQueryList.stream().sorted(Comparator.comparingDouble(ZipCodeQuery::getDistance))
                    .map(ZipCodeQuery::getZipCode).collect(Collectors.toList());

            List<LumIPDO> lumIPDOList = lumIPMapper.selectList(new QueryWrapper<LumIPDO>().lambda()
                .eq(LumIPDO::getStatus, 1).in(LumIPDO::getPostalCode, zipCodeList).le(LumIPDO::getRisk, 80));

            if (CollUtil.isEmpty(lumIPDOList)) {
                rLock.unlock();
                throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的LUM-IP");
            }
            LumIPDO lumIPDO = lumIPDOList.get(0);
            lumIPDO.setStatus(4);
            lumIPMapper.updateById(lumIPDO);

            rLock.unlock();

            return lumIPDO;
        }

        long count = lumIPMapper.selectCount(new QueryWrapper<LumIPDO>().lambda().eq(LumIPDO::getStatus, 1)
            .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCountry()), LumIPDO::getCountry,
                findSocksPortQuery.getCountry())
            .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getState()), LumIPDO::getRegion,
                findSocksPortQuery.getState())
            .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCity()), LumIPDO::getCity,
                findSocksPortQuery.getCity())
            .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getFileType()), LumIPDO::getFileType,
                findSocksPortQuery.getFileType())
            .le(LumIPDO::getRisk, 80)
            .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getIp()), LumIPDO::getIp,
                findSocksPortQuery.getIp())
            .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getZipCode()), LumIPDO::getPostalCode,
                findSocksPortQuery.getZipCode()));
        if (count > 0) {
            int c = RandomUtil.randomInt(0, (int)count);
            LumIPDO lumIPDO = lumIPMapper.selectOne(new QueryWrapper<LumIPDO>().lambda().eq(LumIPDO::getStatus, 1)
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCountry()), LumIPDO::getCountry,
                    findSocksPortQuery.getCountry())
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getState()), LumIPDO::getRegion,
                    findSocksPortQuery.getState())
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCity()), LumIPDO::getCity,
                    findSocksPortQuery.getCity())
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getFileType()), LumIPDO::getFileType,
                    findSocksPortQuery.getFileType())
                .le(LumIPDO::getRisk, 80)
                .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getIp()), LumIPDO::getIp,
                    findSocksPortQuery.getIp())
                .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getZipCode()), LumIPDO::getPostalCode,
                    findSocksPortQuery.getZipCode())
                .last("limit " + c + " , 1"));

            lumIPDO.setStatus(4);
            lumIPMapper.updateById(lumIPDO);

            rLock.unlock();

            return lumIPDO;
        } else {
            rLock.unlock();
            throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的ROLA-IP");
        }
    }

    @Override
    public LumIPDO checkIpActive(LumIPActiveRequest lumIPActiveRequest) {

        LumIPDO lumIPDO = lumIPMapper
            .selectOne(new QueryWrapper<LumIPDO>().lambda().eq(LumIPDO::getIp, lumIPActiveRequest.getSocksAddressIp())
                .eq(LumIPDO::getXLuminatiIp, lumIPActiveRequest.getXLuminatiIp()));
        if (lumIPDO == null) {
            throw new BizException(ReturnCode.RC999.getCode(), "IP不存在");
        }

        String username = StrUtil.format("brd-customer-{}-zone-{}-ip-{}", lumIPActiveRequest.getCustomerUsername(),
            lumIPActiveRequest.getZoneUsername(), lumIPDO.getXLuminatiIp());

        LuminatiIPDTO luminatiIPDTO = null;
        log.info("username {}", username);
        // Proxy proxy = new Proxy(Proxy.Type.SOCKS,
        // new InetSocketAddress(LumIPConstant.LUM_IP_PROXY_HOSTNAME, LumIPConstant.LUM_IP_PROXY_PORT));
        //
        // OkHttpClient client = new OkHttpClient.Builder().proxy(proxy).proxyAuthenticator((route, response) -> {
        // String credential = Credentials.basic(username, lumIPActiveRequest.getZonePassword());
        // return response.request().newBuilder().header("Proxy-Authorization", credential).build();
        // }).build();
        //
        // Request request = new Request.Builder().url("http://lumtest.com/myip.json").build();

        String proxyHost = LumIPConstant.LUM_IP_PROXY_HOSTNAME;
        int proxyPort = LumIPConstant.LUM_IP_PROXY_PORT;
        // 设置代理服务器的认证信息
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(new AuthScope(proxyHost, proxyPort),
            new UsernamePasswordCredentials(username, lumIPActiveRequest.getZonePassword()));

        // 创建 HttpClient 实例，并设置代理服务器信息
        HttpHost proxy = new HttpHost(proxyHost, proxyPort);
        RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
        CloseableHttpClient client =
            HttpClients.custom().setDefaultCredentialsProvider(credsProvider).setDefaultRequestConfig(config).build();
        // 发送 GET 请求
        String url = "http://lumtest.com/myip.json";
        HttpGet request = new HttpGet(url);
        try {

            // okhttp3.Response response = client.newCall(request).execute();
            // String responseString = response.body().string();

            CloseableHttpResponse response = client.execute(request);
            String responseString = EntityUtils.toString(response.getEntity());

            log.info(responseString);
            luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);
        } catch (Exception e) {
            lumIPDO.setStatus(0);
            lumIPMapper.updateById(lumIPDO);
            log.error("lum 测活异常 {}", ExceptionUtil.stacktraceToString(e));
            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest返回失败");
        }
        if (!CharSequenceUtil.equals(lumIPDO.getIp(), luminatiIPDTO.getIp())) {
            lumIPDO.setStatus(0);
            lumIPMapper.updateById(lumIPDO);
            throw new BizException(998, "IP不相等");
        }
        return lumIPDO;
    }

    @Override
    public void collect(LumIPCollectRequest lumIPCollectRequest) {

        RBlockingQueue<LumIPCollectRequest> queue =
            redissonClient.getBlockingQueue(LumIPConstant.LUM_IP_COLLECT_QUEUE_KEY);
        if (!queue.isEmpty()) {
            throw new BizException(ReturnCode.RC999.getCode(), "已有项目在执行，请等待完成后，再次增加收录项目。");
        }

        for (int i = 0; i < lumIPCollectRequest.getNumber(); i++) {
            queue.offer(lumIPCollectRequest);
        }
    }

}
