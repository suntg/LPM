package com.example.lpm.domain.dto;

import java.util.List;

import com.example.lpm.domain.entity.FileDO;
import com.example.lpm.domain.entity.FileIpDO;
import com.example.lpm.domain.entity.FileLogDO;

import lombok.Data;

@Data
public class FileDTO {

    private FileDO file;

    private List<FileLogDO> fileLogList;

    private List<FileIpDO> fileIpList;

    private FileLogDO lastFileLog;

    private FileIpDO lastFileIp;

}
