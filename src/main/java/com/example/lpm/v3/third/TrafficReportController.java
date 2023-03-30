package com.example.lpm.v3.third;


import com.example.lpm.v3.domain.entity.TrafficDO;
import com.example.lpm.v3.service.TrafficService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.Resource;

@Tag(name = "Traffic")
@Slf4j
@Controller
@RequestMapping("/traffic")
public class TrafficReportController {

    @Resource
    private TrafficService trafficService;

    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @GetMapping("/auth")
    public void auth(@RequestParam(name = "user") String user, @RequestParam(name = "pass") Long pass,
                     @RequestParam(name = "client_addr") String clientAddr,
                     @RequestParam(name = "local_addr") String localAddr, @RequestParam(name = "target") String target,
                     @RequestParam(name = "service") String service, @RequestParam(name = "sps") String sps) {

        log.info("user:{},pass:{},client_addr:{},local_addr:{},target:{},service:{},sps:{}", user, pass, clientAddr, localAddr, target, service, sps);

    }


    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    @GetMapping("/report")
    public void report(@RequestParam(name = "act", required = false) String act, @RequestParam(name = "bytes", required = false) Long bytes,
                       @RequestParam(name = "client_addr", required = false) String clientAddr, @RequestParam(name = "id", required = false) String id,
                       @RequestParam(name = "out_local_addr", required = false) String outLocalAddr, @RequestParam(name = "out_remote_addr", required = false) String outRemoteAddr,
                       @RequestParam(name = "server_addr", required = false) String serverAddr, @RequestParam(name = "target_addr", required = false) String targetAddr,
                       @RequestParam(name = "upstream", required = false) String upstream, @RequestParam(name = "username", required = false) String username) {


        log.info("act:{},bytes:{},client_addr:{},id:{},out_local_addr:{},out_remote_addr:{},server_addr:{},target_addr:{},upstream:{},username:{}",
                act, bytes, clientAddr, id, outLocalAddr, outRemoteAddr, serverAddr, targetAddr, upstream, username);

        TrafficDO trafficDO = new TrafficDO();
        trafficDO.setBytes(bytes);
        trafficDO.setUpstream(upstream);
        trafficDO.setClientAddr(clientAddr);
        trafficDO.setOutLocalAddr(outLocalAddr);
        trafficDO.setOutRemoteAddr(outRemoteAddr);
        trafficDO.setServerAddr(serverAddr);
        trafficDO.setTargetAddr(targetAddr);
        trafficDO.setUsername(username);
        trafficDO.setServiceId(id);
        trafficService.report(trafficDO);
    }
}
