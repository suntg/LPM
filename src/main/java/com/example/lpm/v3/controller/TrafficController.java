package com.example.lpm.v3.controller;


import com.example.lpm.v3.domain.query.TrafficStatisticQuery;
import com.example.lpm.v3.domain.vo.TrafficVO;
import com.example.lpm.v3.service.TrafficService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "Traffic")
@Slf4j
@RestController
@RequestMapping("/traffic")
public class TrafficController {


    @Resource
    private TrafficService trafficService;

    @GetMapping("/statistic")
    public List<TrafficVO> statistic(TrafficStatisticQuery trafficStatisticQuery) {
        return trafficService.statistic(trafficStatisticQuery);
    }


}
