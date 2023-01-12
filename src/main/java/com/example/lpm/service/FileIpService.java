package com.example.lpm.service;

import org.springframework.web.bind.annotation.RequestParam;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.domain.entity.FileIpDO;
import com.example.lpm.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.domain.vo.PageVO;

public interface FileIpService extends IService<FileIpDO> {

    PageVO<FileIpDO> listIpsPage(@RequestParam FileQuery fileQuery, PageQuery pageQuery);

}
