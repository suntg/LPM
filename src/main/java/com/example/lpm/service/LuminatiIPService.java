package com.example.lpm.service;

import com.alibaba.fastjson2.JSONArray;
import com.example.lpm.domain.entity.LuminatiIPDO;
import com.example.lpm.domain.request.DeleteProxyPortRequest;
import com.example.lpm.domain.request.LuminatiIPRequest;
import com.example.lpm.domain.request.LuminatiProxyRequest;

@Deprecated
public interface LuminatiIPService {

    LuminatiIPDO getIPAndStartProxy(LuminatiIPRequest luminatiIPRequest);

    void stopProxyByPort(String port);

    void getProxyPort(LuminatiProxyRequest luminatiProxyRequest);

    void deleteProxyPort(DeleteProxyPortRequest deleteProxyPortRequest);

    JSONArray stateProxyPort();
}
