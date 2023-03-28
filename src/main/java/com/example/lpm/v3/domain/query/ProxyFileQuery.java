package com.example.lpm.v3.domain.query;

import com.example.lpm.v3.constant.ProxyIpType;

import lombok.Data;

@Deprecated
@Data
public class ProxyFileQuery {

  private ProxyIpType proxyIpType;

  private String fileType;

  private String fileFlag;

}
