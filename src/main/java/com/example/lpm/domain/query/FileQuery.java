package com.example.lpm.domain.query;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import lombok.Data;

@Deprecated
@Data
public class FileQuery {

    private String fileName;

    private String filePath;

    private String xLuminatiIp;

    private String ip;

    private String logContent;

    private Integer logType;

    private Long fileId;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startCreateTime;
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endCreateTime;
}
