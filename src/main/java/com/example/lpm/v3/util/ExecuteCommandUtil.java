package com.example.lpm.v3.util;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

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

    public static String executeRolaProxySps(Integer socksPort, String socksUsername, String socksPassword,
                                             String socksAddressIp, String rolaUsername, String rolaPassword, String rolaServer, String rolaServerPort) {

        String spsCom = CharSequenceUtil.format("proxy sps -p :{} -a {}:{} -P socks5://{}-ip-{}:{}@{}:{}", socksPort,
                socksUsername, socksPassword, rolaUsername, socksAddressIp, rolaPassword, rolaServer, rolaServerPort);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &");

        log.info("proxy sps command : {}", command);

        RuntimeUtil.execForStr("/bin/bash", "-c", command);

        return spsCom;
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
        return StrUtil.contains(result, proxyCom);
    }

    public static String rolaLocalLumtest(int port, String username, String password) {
        // curl -v --socks5 localhost:5000 -U hotkingda:209209us http://lumtest.com/myip.json
        // wget
        List<String> stringList = RuntimeUtil
                .execForLines("curl -v --socks5 localhost:" + port + " -U " + username + ":" + password + " http://lumtest.com/myip.json");
        log.info(" rolaLocalLumtest curl:{}", JSON.toJSONString(stringList));
        return stringList.get(stringList.size() - 1);
    }
}
