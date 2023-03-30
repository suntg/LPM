package com.example.lpm.util;

import java.io.File;
import java.util.List;

import com.alibaba.fastjson2.JSON;
import com.example.lpm.domain.request.LuminatiIPRequest;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.exceptions.ExceptionUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.file.FileWriter;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
public class ExecuteCommandUtil {

    public static void executeProxySps(LuminatiIPRequest luminatiIPRequest, String xLuminatiIp) {

        // String command = CharSequenceUtil.format("nohup proxy sps -p :{} --luminati --disable-ss -a {}:{} -P "
        // + "http://lum-customer-{}-zone-{}-dns-remote-route_err-pass_dyn-country-{}-ip-{}:{}@{}:22225 >{}.log 2>&1 &",
        // luminatiIPRequest.getSocksPort(), luminatiIPRequest.getSocksUsername(), luminatiIPRequest.getSocksPassword(),
        // luminatiIPRequest.getCustomer(), luminatiIPRequest.getZone(),
        // luminatiIPRequest.getCountry(), xLuminatiIp, luminatiIPRequest.getZonePassword(),
        // luminatiIPRequest.getProxyUrl(), luminatiIPRequest.getSocksPort());
        String command = CharSequenceUtil.format(
            "nohup proxy sps --luminati --lb-method=leastconn --lb-retrytime 300 --lb-timeout 300 -T tcp -a {}:{} -P "
                + " http://lum-customer-{}-zone-{}-dns-remote-route_err-pass_dyn-country-{}-ip-{}:{}@{}:22225 -t tcp -p :{}  >{}.log 2>&1 &",
            luminatiIPRequest.getSocksUsername(), luminatiIPRequest.getSocksPassword(), luminatiIPRequest.getCustomer(),
            luminatiIPRequest.getZone(), luminatiIPRequest.getCountry(), xLuminatiIp,
            luminatiIPRequest.getZonePassword(), luminatiIPRequest.getProxyUrl(), luminatiIPRequest.getSocksPort(),
            luminatiIPRequest.getSocksPort());

        log.info("proxy sps command : {}", command);

        String f = IdUtil.fastUUID() + ".sh";
        File file = FileUtil.touch(FileUtil.getAbsolutePath("../../../") + f);
        FileWriter writer = new FileWriter(file);
        writer.write(command);

        RuntimeUtil.exec("chmod 777 " + FileUtil.getAbsolutePath("../../../"));
        Process result = RuntimeUtil.exec("chmod 777 " + file.getPath());
        log.info("chmod result: {}", RuntimeUtil.getResult(result));
        String bashCommand = "bash " + file.getPath();
        result = RuntimeUtil.exec(bashCommand);
        log.info("bashCommand result: {}", RuntimeUtil.getResult(result));

        file.delete();
    }

    public static String executeRolaProxySps(int port, int userNum, String ip) {

        String spsCom = CharSequenceUtil.format(
            "proxy sps -p :{} -a hotkingda:209209us -P socks5://skyescn_{}-ip-{}:Su902902@gate2.rola.info:2042", port,
            userNum, ip);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &", port);

        log.info("proxy sps command : {}", command);

        String f = IdUtil.fastUUID() + ".sh";
        File file = FileUtil.touch(FileUtil.getAbsolutePath("../../../") + f);
        FileWriter writer = new FileWriter(file);
        writer.write(command);

        RuntimeUtil.exec("chmod 777 " + FileUtil.getAbsolutePath("../../../"));
        Process result = RuntimeUtil.exec("chmod 777 " + file.getPath());
        log.info("chmod result: {}", RuntimeUtil.getResult(result));
        String bashCommand = "bash " + file.getPath();
        result = RuntimeUtil.exec(bashCommand);
        log.info("bashCommand result: {}", RuntimeUtil.getResult(result));

        file.delete();

        return spsCom;
    }

    public static String executeRolaProxySps(Integer socksPort, String socksUsername, String socksPassword,
        String socksAddressIp, String rolaUsername, String rolaPassword) {

        String spsCom =
            CharSequenceUtil.format("proxy sps -p :{} -a {}:{} -P socks5://{}-ip-{}:{}@gate2.rola.info:2042", socksPort,
                socksUsername, socksPassword, rolaUsername, socksAddressIp, rolaPassword);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &", socksPort);

        log.info("proxy sps command : {}", command);

        String f = IdUtil.fastUUID() + ".sh";
        File file = FileUtil.touch(FileUtil.getAbsolutePath("../../../") + f);
        FileWriter writer = new FileWriter(file);
        writer.write(command);

        RuntimeUtil.exec("chmod 777 " + FileUtil.getAbsolutePath("../../../"));
        Process result = RuntimeUtil.exec("chmod 777 " + file.getPath());
        log.info("chmod result: {}", RuntimeUtil.getResult(result));
        String bashCommand = "bash " + file.getPath();
        result = RuntimeUtil.exec(bashCommand);
        log.info("bashCommand result: {}", RuntimeUtil.getResult(result));

        file.delete();

        return spsCom;
    }

    public static boolean rolaProxyState(String proxyCom) {
        String result = RuntimeUtil.execForStr("ps -ef");
        return StrUtil.contains(result, proxyCom);
    }

    public static void killProxyByPort(String port) {
        // 根据端口查询进程编号lsof -i :9001
        String lsofResult = RuntimeUtil.execForStr(CharSequenceUtil.format("lsof -i :{}", port));
        log.info("根据端口查询进程编号:{}", lsofResult);
        // 再通过编号杀死进程 kill -9 3340
        List<String> lsofResultList = CharSequenceUtil.splitTrim(lsofResult, " ");
        log.info(JSON.toJSONString(lsofResultList));
        if (CollUtil.isNotEmpty(lsofResultList)) {
            String processNumber = CharSequenceUtil.splitTrim(lsofResult, " ").get(9);
            log.info(processNumber);
            RuntimeUtil.execForStr(CharSequenceUtil.format("kill -9 {}", processNumber));
            try {
                FileUtil.del(FileUtil.getAbsolutePath("../../../") + port + ".log");
            } catch (IORuntimeException e) {
                log.error("File del:{}", ExceptionUtil.stacktraceToString(e));
            }
        }
    }

    public static void killRolaProxyByPort(String port) {
        // 根据端口查询进程编号lsof -i :9001
        List<String> lsofResult = RuntimeUtil.execForLines(CharSequenceUtil.format("lsof -i :{}", port));
        log.info("根据端口查询进程编号:{}", lsofResult);

        for (String s : lsofResult) {
            List<String> list = CharSequenceUtil.splitTrim(s, " ");
            if (list.contains("*:" + port) && list.contains("proxy")) {
                log.info("processNumber:{}", list.get(1));
                RuntimeUtil.execForStr(CharSequenceUtil.format("kill -9 {}", list.get(1)));
            }
        }
    }

    public static String rolaRefresh(String user, String country, String state, String city) {

        // http://refresh.rola.info/refresh?user=skyescn_1&country=us&state=&city=
        //http://refreshus2.rola.info/refresh?user=skyescn_1&country=us&state=&city=
        StringBuilder rolaUrl = new StringBuilder("http://refreshus2.rola.info/refresh?user=" + user + "&country=");
        rolaUrl.append(StrUtil.replace(country, " ", "").toLowerCase());
        rolaUrl.append("&state=");
        if (StrUtil.isNotBlank(state)) {
            rolaUrl.append(StrUtil.replace(state, " ", "").toLowerCase());
        }
        rolaUrl.append("&city=");
        if (StrUtil.isNotBlank(city)) {
            rolaUrl.append(StrUtil.replace(city, " ", "").toLowerCase());
        }
        log.info(" rola url :{}", rolaUrl);
        List<String> stringList = RuntimeUtil.execForLines("curl " + rolaUrl);

        log.info(" rolaRefresh curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }

    public static String rolaLumtest(String user) {
        // curl -v --socks5 proxyus.rola.info:2042 -U skyescn_1:209209us http://lumtest.com/myip.json
        List<String> stringList = RuntimeUtil.execForLines(
            "curl -v --socks5 gate2.rola.info:2042 -U " + user + ":209209us http://lumtest.com/myip.json");
        log.info(" rolaLumtest curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }

    public static String rolaLocalLumtest(int port) {
        // curl -v --socks5 localhost:5000 -U hotkingda:209209us http://lumtest.com/myip.json
        // wget
        List<String> stringList = RuntimeUtil
            .execForLines("curl -v --socks5 localhost:" + port + " -U hotkingda:209209us http://lumtest.com/myip.json");
        log.info(" rolaLocalLumtest curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }

}
