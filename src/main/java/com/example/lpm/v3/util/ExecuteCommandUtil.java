package com.example.lpm.v3.util;

import java.util.List;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.RuntimeUtil;
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

    public static String executeRolaProxySps(Integer socksPort, String socksUsername, String socksPassword,
        String socksAddressIp, String rolaUsername, String rolaPassword, String rolaServer, String rolaServerPort) {

        String spsCom = CharSequenceUtil.format("proxy sps -p :{} -a {}:{} -P socks5://{}-ip-{}:{}@{}:{}", socksPort,
            socksUsername, socksPassword, rolaUsername, socksAddressIp, rolaPassword, rolaServer, rolaServerPort);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &");

        log.info("proxy sps command : {}", command);

        RuntimeUtil.execForStr("/bin/bash", "-c", command);

        return spsCom;
    }

    public static String executeLumProxySps(Integer socksPort, String socksUsername, String socksPassword,
        String username, String password, String server, String serverPort) {

        String spsCom = CharSequenceUtil.format("proxy sps -p :{} --luminati --disable-ss -a {}:{} -P {}:{}@{}:{}",
            socksPort, socksUsername, socksPassword, username, password, server, serverPort);

        String command = CharSequenceUtil.format("nohup " + spsCom + "  >/dev/null 2>&1 &");

        log.info("proxy sps command : {}", command);

        RuntimeUtil.execForStr("/bin/bash", "-c", command);

        return spsCom;
    }

}
