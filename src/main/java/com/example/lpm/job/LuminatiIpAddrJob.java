package com.example.lpm.job;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.lpm.domain.entity.IpAddrDO;
import com.example.lpm.service.IpAddrService;
import com.xxl.job.core.context.XxlJobHelper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LuminatiIpAddrJob {

    @Resource
    private IpAddrService ipAddrService;

    // @XxlJob("getLuminatiIpJobHandler")
    public void getLuminatiIpJobHandler() throws Exception {
        XxlJobHelper.log("XXL-JOB, GetLuminatiIpJobHandler");
        String param = XxlJobHelper.getJobParam();
        int number = Integer.parseInt(param);
        for (int i = 0; i < number; i++) {
            ipAddrService.getLuminatiIpAddrByAsync(null, null, null);
        }
    }

    // @XxlJob("heartbeatJobHandler")
    public void heartbeatJobHandler() throws Exception {
        XxlJobHelper.log("XXL-JOB, HeartbeatJobHandler");
        List<IpAddrDO> ipAddrDOList = ipAddrService.getBaseMapper()
            .selectList(new QueryWrapper<IpAddrDO>().lambda().select(IpAddrDO::getId).ne(IpAddrDO::getState, 0));
        for (IpAddrDO ipAddrDO : ipAddrDOList) {
            ipAddrService.heartbeatAsync(ipAddrDO.getId());
        }
    }

}
