package com.example.lpm.domain.request;

import java.util.List;

import lombok.Data;

@Deprecated
@Data
public class DeleteProxyPortRequest {

    private String server;

    private List<Integer> ports;

}
