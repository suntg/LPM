package com.example.lpm.v3.controller;


import com.example.lpm.v3.domain.query.TrafficStatisticQuery;
import com.example.lpm.v3.service.TrafficService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "Traffic")
@Slf4j
@RestController
@RequestMapping("/traffic")
public class TrafficController {


    @Resource
    private TrafficService trafficService;

    @GetMapping("/statistic")
    public Long statistic( TrafficStatisticQuery trafficStatisticQuery) {
        return trafficService.statistic(trafficStatisticQuery);
    }

}
