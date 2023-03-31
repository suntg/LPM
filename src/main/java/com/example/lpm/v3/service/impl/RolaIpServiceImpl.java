package com.example.lpm.v3.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.text.csv.CsvData;
import cn.hutool.core.text.csv.CsvReader;
import cn.hutool.core.text.csv.CsvRow;
import cn.hutool.core.text.csv.CsvUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.constant.ProxyConstant;
import com.example.lpm.constant.RedisKeyConstant;
import com.example.lpm.domain.dto.LuminatiIPDTO;
import com.example.lpm.v3.common.BizException;
import com.example.lpm.v3.common.ReturnCode;
import com.example.lpm.v3.config.GzipRequestInterceptor;
import com.example.lpm.v3.domain.entity.RolaIpDO;
import com.example.lpm.v3.domain.query.FindSocksPortQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.query.RolaQuery;
import com.example.lpm.v3.domain.query.ZipCodeQuery;
import com.example.lpm.v3.domain.request.RolaIpActiveRequest;
import com.example.lpm.v3.domain.request.RolaIpLockRequest;
import com.example.lpm.v3.domain.request.RolaIpRequest;
import com.example.lpm.v3.domain.vo.PageVO;
import com.example.lpm.v3.domain.vo.RolaProgressVO;
import com.example.lpm.v3.mapper.RolaIpMapper;
import com.example.lpm.v3.service.RolaIpService;
import com.example.lpm.v3.util.RolaUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RolaIpServiceImpl extends ServiceImpl<RolaIpMapper, RolaIpDO> implements RolaIpService {

    private final ObjectMapper objectMapper;

    private final RolaIpMapper rolaIpMapper;

    private final RedissonClient redissonClient;

    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void collect(RolaIpRequest rolaIpRequest) {

        // 在收录代理的时候,先判断城市列表是存在.如果存在在进行收录(网页上)
        // 如果不存在情况下,网页提示该州不在收录范围内.
        if ("us".equalsIgnoreCase(rolaIpRequest.getCountry())) {
            ClassPathResource classPathResource = new ClassPathResource("country-state-city.csv");
            try {
                InputStream inputStream = classPathResource.getInputStream();
                CsvReader reader = CsvUtil.getReader();
                CsvData data = reader.read(new InputStreamReader(inputStream, "UTF-8"));
                List<CsvRow> rows = data.getRows();

                String state = null;
                if (StrUtil.isNotBlank(rolaIpRequest.getState())) {
                    state = StrUtil.replace(rolaIpRequest.getState(), " ", "").toLowerCase();
                }

                String city = null;
                if (StrUtil.isNotBlank(rolaIpRequest.getCity())) {
                    city = StrUtil.replace(rolaIpRequest.getCity(), " ", "").toLowerCase();
                }


                Integer stateFlag = 0;
                Integer cityFlag = 0;

                for (CsvRow row : rows) {
                    List<String> list = row.getRawList();
                    if (StrUtil.isNotBlank(state) && list.contains(state)) {
                        stateFlag = 1;
                    }
                    if (StrUtil.isNotBlank(city) && list.contains(city)) {
                        cityFlag = 1;
                    }
                }
                if (StrUtil.isNotBlank(state) && stateFlag == 0) {
                    throw new BizException(ReturnCode.RC999.getCode(), "该地址不在收录范围内");
                }
                if (StrUtil.isNotBlank(city) && cityFlag == 0) {
                    throw new BizException(ReturnCode.RC999.getCode(), "该地址不在收录范围内");
                }
            } catch (IOException e) {
                throw new BizException(ReturnCode.RC999.getCode(), "打开罗拉代理的州和城市列表失败");
            }
        }

        // 判断 队列数量，>0拒绝任务
        RBlockingQueue<RolaIpRequest> queue =
                redissonClient.getBlockingQueue(RedisKeyConstant.ROLA_COLLECT_IP_QUEUE_KEY);
        if (queue.size() > 0) {
            throw new BizException(ReturnCode.RC999.getCode(), "已有项目在执行，请等待完成后，再次增加收录项目。");
        }

        // 暂停，开始
        RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_COLLECT_FLAG_KEY);
        // 开始 10
        collectFlag.set(10L);

        // 当前任务适量
        RAtomicLong currentNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_KEY);
        currentNum.set(rolaIpRequest.getNumber());

        // 放入队列
        for (int i = 0; i < rolaIpRequest.getNumber(); i++) {
            boolean result = queue.offer(rolaIpRequest);
        }

        redisTemplate.delete(RedisKeyConstant.ROLA_COLLECT_ERROR_KEY);
    }

    @Override
    public void phoneCollect(RolaIpRequest rolaIpRequest) {
        RBlockingQueue<RolaIpRequest> queue =
                redissonClient.getBlockingQueue(RedisKeyConstant.ROLA_PHONE_COLLECT_IP_QUEUE_KEY);
        for (int i = 0; i < rolaIpRequest.getNumber(); i++) {
            queue.offer(rolaIpRequest);
        }
    }

    @Override
    public void endCollect() {
        RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_COLLECT_FLAG_KEY);
        collectFlag.set(11L);
        RBlockingQueue<RolaIpRequest> queue =
                redissonClient.getBlockingQueue(RedisKeyConstant.ROLA_COLLECT_IP_QUEUE_KEY);
        queue.clear();
    }

    @Override
    public void pauseCollect() {
        RAtomicLong collectFlag = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_COLLECT_FLAG_KEY);
        collectFlag.set(11L);
    }

    @Override
    public RolaProgressVO collectProgress() {
        RAtomicLong currentNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_KEY);

        RAtomicLong totalNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_TOTAL_KEY);

        String today = DateUtil.today();
        RAtomicLong todayNum = redissonClient.getAtomicLong("#ROLA_" + today);

        RBlockingQueue<RolaIpRequest> queue =
                redissonClient.getBlockingQueue(RedisKeyConstant.ROLA_COLLECT_IP_QUEUE_KEY);

        RAtomicLong currentRepeatNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_REPEAT_KEY);

        RAtomicLong currentFailNum = redissonClient.getAtomicLong(RedisKeyConstant.ROLA_CURRENT_FAIL_KEY);

        String error = redisTemplate.opsForValue().get(RedisKeyConstant.ROLA_COLLECT_ERROR_KEY);

        RolaProgressVO rolaProgressVO = new RolaProgressVO();
        rolaProgressVO.setCurrentNum(currentNum.get());
        rolaProgressVO.setTotalNum(totalNum.get());
        rolaProgressVO.setTodayNum(todayNum.get());
        rolaProgressVO.setCompletedNum(rolaProgressVO.getCurrentNum() - queue.size());
        rolaProgressVO.setCurrentFailNum(currentFailNum.get());
        rolaProgressVO.setCurrentRepeatNum(currentRepeatNum.get());
        rolaProgressVO.setError(error);

        return rolaProgressVO;
    }

    @Override
    public PageVO<RolaIpDO> listRolaIpsPage(RolaQuery rolaQuery, PageQuery pageQuery) {
        if (CharSequenceUtil.isNotBlank(rolaQuery.getCountry())) {
            rolaQuery.setCountry(rolaQuery.getCountry().toLowerCase());
        }
        if (CharSequenceUtil.isNotBlank(rolaQuery.getState())) {
            rolaQuery.setState(rolaQuery.getState().toLowerCase());
        }
        if (CharSequenceUtil.isNotBlank(rolaQuery.getCity())) {
            rolaQuery.setCity(rolaQuery.getCity().toLowerCase());
        }

        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());

        List<RolaIpDO> rolaIpDOList =
                rolaIpMapper.selectList(new QueryWrapper<RolaIpDO>().lambda()
                        .ne(RolaIpDO::getStatus, 3)
                        .eq(CharSequenceUtil.isNotBlank(rolaQuery.getCountry()), RolaIpDO::getCountry, rolaQuery.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(rolaQuery.getState()), RolaIpDO::getRegion, rolaQuery.getState())
                        .eq(CharSequenceUtil.isNotBlank(rolaQuery.getCity()), RolaIpDO::getCity, rolaQuery.getCity())
                        .likeRight(CharSequenceUtil.isNotBlank(rolaQuery.getIp()), RolaIpDO::getIp, rolaQuery.getIp())
                        .likeRight(CharSequenceUtil.isNotBlank(rolaQuery.getZipCode()), RolaIpDO::getPostalCode,
                                rolaQuery.getZipCode())
                        .orderByDesc(RolaIpDO::getCreateTime));
        if (CollUtil.isNotEmpty(rolaIpDOList)) {
            for (RolaIpDO rolaIpDO : rolaIpDOList) {
                if (CharSequenceUtil.isNotBlank(rolaIpDO.getCity())) {
                    StringBuilder city = new StringBuilder();
                    for (String s : CharSequenceUtil.split(rolaIpDO.getCity(), " ")) {
                        city.append(CharSequenceUtil.upperFirst(s)).append(" ");
                    }
                    rolaIpDO.setCity(CharSequenceUtil.trim(city.toString()));
                }
                if (CharSequenceUtil.isNotBlank(rolaIpDO.getRegion())) {
                    rolaIpDO.setRegion(rolaIpDO.getRegion().toUpperCase());
                }
                if (CharSequenceUtil.isNotBlank(rolaIpDO.getCountry())) {
                    rolaIpDO.setCountry(rolaIpDO.getCountry().toUpperCase());
                }
            }
        }

        return new PageVO<>(page.getTotal(), rolaIpDOList);
    }

    @Override
    public PageVO<RolaIpDO> listFilesPage(RolaQuery rolaQuery, PageQuery pageQuery) {
        if (CharSequenceUtil.isNotBlank(rolaQuery.getCountry())) {
            rolaQuery.setCountry(rolaQuery.getCountry().toLowerCase());
        }
        if (CharSequenceUtil.isNotBlank(rolaQuery.getState())) {
            rolaQuery.setState(rolaQuery.getState().toLowerCase());
        }
        if (CharSequenceUtil.isNotBlank(rolaQuery.getCity())) {
            rolaQuery.setCity(rolaQuery.getCity().toLowerCase());
        }

        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());

        List<RolaIpDO> rolaIpDOList =
                rolaIpMapper.selectList(new QueryWrapper<RolaIpDO>().lambda()
                        .ne(RolaIpDO::getStatus, 3)
                        .like(CharSequenceUtil.isNotBlank(rolaQuery.getFileName()), RolaIpDO::getFileFlag, rolaQuery.getFileName())
                        .isNotNull(RolaIpDO::getFileFlag)
                        .eq(CharSequenceUtil.isNotBlank(rolaQuery.getCountry()), RolaIpDO::getCountry, rolaQuery.getCountry())
                        .eq(CharSequenceUtil.isNotBlank(rolaQuery.getState()), RolaIpDO::getRegion, rolaQuery.getState())
                        .eq(CharSequenceUtil.isNotBlank(rolaQuery.getCity()), RolaIpDO::getCity, rolaQuery.getCity())
                        .likeRight(CharSequenceUtil.isNotBlank(rolaQuery.getIp()), RolaIpDO::getIp, rolaQuery.getIp())
                        .likeRight(CharSequenceUtil.isNotBlank(rolaQuery.getZipCode()), RolaIpDO::getPostalCode,
                                rolaQuery.getZipCode())
                        .orderByDesc(RolaIpDO::getCreateTime));
        if (CollUtil.isNotEmpty(rolaIpDOList)) {
            for (RolaIpDO rolaIpDO : rolaIpDOList) {
                if (CharSequenceUtil.isNotBlank(rolaIpDO.getCity())) {
                    StringBuilder city = new StringBuilder();
                    for (String s : CharSequenceUtil.split(rolaIpDO.getCity(), " ")) {
                        city.append(CharSequenceUtil.upperFirst(s)).append(" ");
                    }
                    rolaIpDO.setCity(CharSequenceUtil.trim(city.toString()));
                }
                if (CharSequenceUtil.isNotBlank(rolaIpDO.getRegion())) {
                    rolaIpDO.setRegion(rolaIpDO.getRegion().toUpperCase());
                }
                if (CharSequenceUtil.isNotBlank(rolaIpDO.getCountry())) {
                    rolaIpDO.setCountry(rolaIpDO.getCountry().toUpperCase());
                }
            }
        }

        return new PageVO<>(page.getTotal(), rolaIpDOList);
    }

    @Override
    public RolaIpDO findSocksPort(FindSocksPortQuery findSocksPortQuery) {
        // 加锁 lock("#ROLA_FIND_LOCK")
        RLock rLock = redissonClient.getLock(RedisKeyConstant.ROLA_FIND_LOCK);
        if (rLock.isLocked()) {
            log.error("ROLA_FIND_LOCK获取锁失败:{}", RedisKeyConstant.ROLA_FIND_LOCK);
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

            List<RolaIpDO> rolaIpDOList = rolaIpMapper.selectList(new QueryWrapper<RolaIpDO>().lambda()
                    .eq(RolaIpDO::getStatus, 1).in(RolaIpDO::getPostalCode, zipCodeList).le(RolaIpDO::getRisk, 80));

            if (CollUtil.isEmpty(rolaIpDOList)) {
                rLock.unlock();
                throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的ROLA-IP");
            }

            RolaIpDO rolaIpDO = rolaIpDOList.get(0);

            rolaIpDO.setStatus(4);
            rolaIpMapper.updateById(rolaIpDO);

            rLock.unlock();

            return rolaIpDO;
        }

        long count = rolaIpMapper.selectCount(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getStatus, 1)
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCountry()), RolaIpDO::getCountry,
                        findSocksPortQuery.getCountry())
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getState()), RolaIpDO::getRegion,
                        findSocksPortQuery.getState())
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCity()), RolaIpDO::getCity,
                        findSocksPortQuery.getCity())
                .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getFileType()), RolaIpDO::getFileType,
                        findSocksPortQuery.getFileType())
                .le(RolaIpDO::getRisk, 80)
                .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getIp()), RolaIpDO::getIp,
                        findSocksPortQuery.getIp())
                .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getZipCode()), RolaIpDO::getPostalCode,
                        findSocksPortQuery.getZipCode()));
        if (count > 0) {
            int c = RandomUtil.randomInt(0, (int) count);
            RolaIpDO rolaIpDO = rolaIpMapper.selectOne(new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getStatus, 1)
                    .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCountry()), RolaIpDO::getCountry,
                            findSocksPortQuery.getCountry())
                    .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getState()), RolaIpDO::getRegion,
                            findSocksPortQuery.getState())
                    .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getCity()), RolaIpDO::getCity,
                            findSocksPortQuery.getCity())
                    .eq(CharSequenceUtil.isNotBlank(findSocksPortQuery.getFileType()), RolaIpDO::getFileType,
                            findSocksPortQuery.getFileType())
                    .le(RolaIpDO::getRisk, 80)
                    .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getIp()), RolaIpDO::getIp,
                            findSocksPortQuery.getIp())
                    .likeRight(CharSequenceUtil.isNotBlank(findSocksPortQuery.getZipCode()), RolaIpDO::getPostalCode,
                            findSocksPortQuery.getZipCode())
                    .last("limit " + c + " , 1"));

            rolaIpDO.setStatus(4);
            rolaIpMapper.updateById(rolaIpDO);

            rLock.unlock();

            return rolaIpDO;
        } else {
            rLock.unlock();
            throw new BizException(ReturnCode.RC999.getCode(), "没有符合条件的ROLA-IP");
        }

    }

    @Override
    public void submitIpLock(RolaIpLockRequest rolaIpLockRequest) {
        rolaIpMapper.update(new RolaIpDO(),
                new UpdateWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, rolaIpLockRequest.getSocksAddressIp())
                        .set(CharSequenceUtil.isNotBlank(rolaIpLockRequest.getFileFlag()), RolaIpDO::getFileFlag,
                                rolaIpLockRequest.getFileFlag())
                        .set(CharSequenceUtil.isNotBlank(rolaIpLockRequest.getFileType()), RolaIpDO::getFileType,
                                rolaIpLockRequest.getFileType())
                        .set(RolaIpDO::getStatus, rolaIpLockRequest.getStatus()));
    }

    @Override
    public RolaIpDO checkIpActive(RolaIpActiveRequest rolaIpActiveRequest) throws Exception {

        RolaIpDO rolaIpDO = rolaIpMapper.selectOne(
                new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, rolaIpActiveRequest.getSocksAddressIp()));
        if (rolaIpDO == null) {
            throw new BizException(ReturnCode.RC999.getCode(), "IP不存在");
        }

        if (CharSequenceUtil.isBlank(rolaIpActiveRequest.getRolaUsername())) {
            String user = RolaUtil.randomUsername();
            rolaIpActiveRequest.setRolaUsername(user);
        }
        if (CharSequenceUtil.isBlank(rolaIpActiveRequest.getRolaPassword())) {
            rolaIpActiveRequest.setRolaPassword("Su902902");
        }

        String rolaUsername = rolaIpActiveRequest.getRolaUsername() + "-ip-" + rolaIpDO.getIp();
        LuminatiIPDTO luminatiIPDTO = null;

        Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("gate2.rola.info", 2042));
        java.net.Authenticator.setDefault(new java.net.Authenticator() {
            private final PasswordAuthentication authentication =
                    new PasswordAuthentication(rolaUsername, rolaIpActiveRequest.getRolaPassword().toCharArray());

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
        log.info(responseString);
        try {
            luminatiIPDTO = objectMapper.readValue(responseString, LuminatiIPDTO.class);
        } catch (Exception e) {
            rolaIpDO.setStatus(0);
            rolaIpMapper.updateById(rolaIpDO);
            throw new BizException(ReturnCode.RC999.getCode(), "调用lumtest返回失败");
        }
        if (!CharSequenceUtil.equals(rolaIpDO.getIp(), luminatiIPDTO.getIp())) {
            rolaIpDO.setStatus(0);
            rolaIpMapper.updateById(rolaIpDO);
            throw new BizException(998, "IP不相等");
        }

        Request request1 = new Request.Builder().url("https://www.paypal.com/signin").build();
        okhttp3.Response response1 = client.newCall(request1).execute();
        String responseString1 = response1.body().string();
        log.info("paypal http code:[]", response1.code());
        if (StrUtil.contains(responseString1, "403")) {
            rolaIpDO.setStatus(0);
            rolaIpMapper.updateById(rolaIpDO);
            throw new BizException(ReturnCode.RC999.getCode(), "调用paypal返回403");
        }
        return rolaIpDO;
    }

    @Override
    public List<RolaIpDO> listByFileFlag(String fileFlag, String fileType) {
        return rolaIpMapper.selectList(new QueryWrapper<RolaIpDO>().lambda().like(RolaIpDO::getFileFlag, fileFlag)
                .like(CharSequenceUtil.isNotBlank(fileType), RolaIpDO::getFileType, fileType));
    }

    @Override
    public RolaIpDO checkIpLock(RolaIpLockRequest rolaIpLockRequest) {
        RolaIpDO rolaIpDO = rolaIpMapper.selectOne(
                new QueryWrapper<RolaIpDO>().lambda().eq(RolaIpDO::getIp, rolaIpLockRequest.getSocksAddressIp()));
        if (rolaIpDO != null) {
            return rolaIpDO;
        } else {
            throw new BizException(ReturnCode.RC999.getCode(), "数据不存在");
        }
    }

}
