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
    @GetMapping("/report")
    public void report(@RequestParam(name = "act") String act, @RequestParam(name = "bytes") Long bytes,
                       @RequestParam(name = "client_addr") String clientAddr, @RequestParam(name = "id") String id,
                       @RequestParam(name = "out_local_addr") String outLocalAddr, @RequestParam(name = "out_remote_addr") String outRemoteAddr,
                       @RequestParam(name = "server_addr") String serverAddr, @RequestParam(name = "target_addr") String targetAddr,
                       @RequestParam(name = "upstream") String upstream, @RequestParam(name = "username") String username) {

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
