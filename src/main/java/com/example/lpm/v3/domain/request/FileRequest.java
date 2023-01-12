package com.example.lpm.v3.domain.request;

import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class FileRequest {

    @NotNull(message = "fileName不能为空")
    private String fileName;

    private String filePath;

    private String xLuminatiIp;

    private String ip;

    private String logContent;

}
