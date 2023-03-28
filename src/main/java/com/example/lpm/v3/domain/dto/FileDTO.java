package com.example.lpm.v3.domain.dto;

import java.util.List;

import com.example.lpm.v3.domain.entity.FileDO;
import com.example.lpm.v3.domain.entity.FileIpDO;
import com.example.lpm.v3.domain.entity.FileLogDO;

import lombok.Data;

@Data
public class FileDTO {

    private FileDO file;

    private List<FileLogDO> fileLogList;

    private List<FileIpDO> fileIpList;

    private FileLogDO lastFileLog;

    private FileIpDO lastFileIp;

}
