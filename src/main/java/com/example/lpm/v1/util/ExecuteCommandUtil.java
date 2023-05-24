package com.example.lpm.v1.util;

import java.util.List;

import com.alibaba.fastjson2.JSON;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExecuteCommandUtil {

    /**
     * 根据端口杀进程
     *
     * @param port
     */
    public static void killProcessByPort(int port) {
        // 根据端口查询进程编号lsof -i :9001
        String lsofResult = RuntimeUtil.execForStr(CharSequenceUtil.format("lsof -i :{}", port));
        log.info("根据端口查询进程编号:{}", lsofResult);
        // 再通过编号杀死进程 kill -9 3340
        List<String> lsofResultList = CharSequenceUtil.splitTrim(lsofResult, " ");
        if (CollUtil.isNotEmpty(lsofResultList)) {
            String processNumber = CharSequenceUtil.splitTrim(lsofResult, " ").get(9);
            log.info("根据端口杀进程编号:{}", processNumber);
            RuntimeUtil.execForStr(CharSequenceUtil.format("kill -9 {}", processNumber));
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

    public static String executeRolaProxySps(Integer socksPort, String socksUsername, String socksPassword,
        String socksAddressIp, String rolaUsername, String rolaPassword) {

        String spsCom = CharSequenceUtil.format(
            "proxy sps -p :{} -a {}:{} -P socks5://{}-ip-{}:{}@gate2.rola.info:2042 --traffic-url http://localhost:21888/traffic/report --traffic-mode fast ",
            socksPort, socksUsername, socksPassword, rolaUsername, socksAddressIp, rolaPassword);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &", socksPort);

        log.info("proxy sps command : {}", command);

        RuntimeUtil.execForStr("/bin/bash", "-c", command);

        return spsCom;
    }

    @Deprecated
    public static String executeRolaProxySps(Integer socksPort, String socksUsername, String socksPassword,
        String socksAddressIp, String rolaUsername, String rolaPassword, String rolaServer, String rolaServerPort) {

        String spsCom = CharSequenceUtil.format("proxy sps -p :{} -a {}:{} -P socks5://{}-ip-{}:{}@{}:{}", socksPort,
            socksUsername, socksPassword, rolaUsername, socksAddressIp, rolaPassword, rolaServer, rolaServerPort);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &");

        log.info("proxy sps command : {}", command);

        RuntimeUtil.execForStr("/bin/bash", "-c", command);

        return spsCom;
    }

    public static String executeRolaProxySps(int port, int userNum, String ip) {

        String spsCom = CharSequenceUtil.format(
            "proxy sps -p :{} -a hotkingda:209209us -P socks5://skyescn_{}-ip-{}:Su902902@gate2.rola.info:2042", port,
            userNum, ip);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &", port);

        log.info("proxy sps command : {}", command);

        RuntimeUtil.execForStr("/bin/bash", "-c", command);

        return spsCom;
    }

    public static String rolaLocalLumtest(int port) {
        // curl -v --socks5 localhost:5000 -U hotkingda:209209us http://lumtest.com/myip.json
        // wget
        List<String> stringList = RuntimeUtil
            .execForLines("curl -v --socks5 localhost:" + port + " -U hotkingda:209209us http://lumtest.com/myip.json");
        log.info(" rolaLocalLumtest curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }

    @Deprecated
    public static String executeLumProxySps(Integer socksPort, String socksUsername, String socksPassword,
        String username, String password, String server, String serverPort) {

        String spsCom = CharSequenceUtil.format("proxy sps -p :{} --luminati --disable-ss -a {}:{} -P {}:{}@{}:{}",
            socksPort, socksUsername, socksPassword, username, password, server, serverPort);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &");

        log.info("proxy sps command : {}", command);

        RuntimeUtil.execForStr("/bin/bash", "-c", command);

        return spsCom;
    }

    public static boolean proxyState(String proxyCom) {
        String result = RuntimeUtil.execForStr("ps -ef");
        return CharSequenceUtil.contains(result, proxyCom);
    }

    public static String rolaLocalLumtest(int port, String username, String password) {
        // curl -v --socks5 localhost:5000 -U hotkingda:209209us http://lumtest.com/myip.json
        // wget
        List<String> stringList = RuntimeUtil.execForLines("curl -v --socks5 localhost:" + port + " -U " + username
            + ":" + password + " http://lumtest.com/myip.json");
        log.info(" rolaLocalLumtest curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }

    public static String rolaLumtest(String user) {
        // curl -v --socks5 proxyus.rola.info:2042 -U skyescn_1:209209us http://lumtest.com/myip.json
        List<String> stringList = RuntimeUtil.execForLines(
            "curl -v --socks5 gate2.rola.info:2042 -U " + user + ":209209us http://lumtest.com/myip.json");
        log.info(" rolaLumtest curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }

    public static String rolaRefresh(String user, String country, String state, String city) {

        // http://refresh.rola.info/refresh?user=skyescn_1&country=us&state=&city=
        // http://refreshus2.rola.info/refresh?user=skyescn_1&country=us&state=&city=
        StringBuilder rolaUrl = new StringBuilder("http://refreshus2.rola.info/refresh?user=" + user + "&country=");
        rolaUrl.append(StrUtil.replace(country, " ", "").toLowerCase());
        rolaUrl.append("&state=");
        if (CharSequenceUtil.isNotBlank(state)) {
            rolaUrl.append(CharSequenceUtil.replace(state, " ", "").toLowerCase());
        }
        rolaUrl.append("&city=");
        if (CharSequenceUtil.isNotBlank(city)) {
            rolaUrl.append(CharSequenceUtil.replace(city, " ", "").toLowerCase());
        }
        log.info(" rola url :{}", rolaUrl);
        List<String> stringList = RuntimeUtil.execForLines("curl " + rolaUrl);

        log.info(" rolaRefresh curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }

    public static boolean rolaProxyState(String proxyCom) {
        String result = RuntimeUtil.execForStr("ps -ef");
        return CharSequenceUtil.contains(result, proxyCom);
    }

    public static boolean portOccupancy(int port) {
        String result = RuntimeUtil.execForStr("/bin/bash", "-c", "lsof -i:" + port);
        return CharSequenceUtil.isNotBlank(result);
    }
}
