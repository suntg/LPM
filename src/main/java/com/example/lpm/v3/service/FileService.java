package com.example.lpm.v3.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.lpm.v3.domain.dto.FileDTO;
import com.example.lpm.v3.domain.entity.FileDO;
import com.example.lpm.v3.domain.query.FileQuery;
import com.example.lpm.v3.domain.query.PageQuery;
import com.example.lpm.v3.domain.request.FileRequest;
import com.example.lpm.v3.domain.vo.PageVO;

public interface FileService extends IService<FileDO> {

    PageVO<FileDTO> listPage(FileQuery fileQuery, PageQuery pageQuery);

    void saveFile(FileRequest fileRequest);

    FileDTO getFile(String fileName, Long fileId);

    List<FileDTO> listFiles(List<Long> fileIds);
}
