package com.example.lpm.domain.request;

import lombok.Data;

@Data
public class FileRequest {

    private String fileName;

    private String filePath;

    private String xLuminatiIp;

    private String ip;

    private String logContent;

}
