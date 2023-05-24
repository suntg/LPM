package com.example.lpm.v1.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.lpm.v1.common.BizException;
import com.example.lpm.v1.common.ReturnCode;
import com.example.lpm.v1.domain.dto.FileDTO;
import com.example.lpm.v1.domain.entity.FileDO;
import com.example.lpm.v1.domain.entity.FileIpDO;
import com.example.lpm.v1.domain.entity.FileLogDO;
import com.example.lpm.v1.domain.query.FileQuery;
import com.example.lpm.v1.domain.query.PageQuery;
import com.example.lpm.v1.domain.request.FileRequest;
import com.example.lpm.v1.domain.vo.PageVO;
import com.example.lpm.v1.mapper.FileIpMapper;
import com.example.lpm.v1.mapper.FileLogMapper;
import com.example.lpm.v1.mapper.FileMapper;
import com.example.lpm.v1.service.FileService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.ObjectUtil;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FileServiceImpl extends ServiceImpl<FileMapper, FileDO> implements FileService {

    @Resource
    private FileMapper fileMapper;

    @Resource
    private FileLogMapper fileLogMapper;

    @Resource
    private FileIpMapper fileIpMapper;

    @Resource
    private RedissonClient redissonClient;

    @Override
    public PageVO<FileDTO> listPage(FileQuery fileQuery, PageQuery pageQuery) {
        // 文件名 & 文件路径 & 日志内容（模糊查询） xlumip、ip
        if (CharSequenceUtil.isAllBlank(fileQuery.getLogContent(), fileQuery.getXLuminatiIp(), fileQuery.getIp())) {
            Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
            List<FileDO> fileDOList = fileMapper.selectList(new QueryWrapper<FileDO>().lambda()
                .like(CharSequenceUtil.isNotBlank(fileQuery.getFileName()), FileDO::getName, fileQuery.getFileName())
                .eq(CharSequenceUtil.isNotBlank(fileQuery.getFilePath()), FileDO::getPath, fileQuery.getFilePath())
                .orderByDesc(FileDO::getCreateTime));
            List<FileDTO> fileDTOList = new ArrayList<>();

            for (FileDO fileDO : fileDOList) {
                FileDTO fileDTO = new FileDTO();
                fileDTO.setFile(fileDO);

                FileLogDO fileLogDO = fileLogMapper.selectOne(new QueryWrapper<FileLogDO>().lambda()
                    .eq(FileLogDO::getFileId, fileDO.getId()).orderByDesc(FileLogDO::getCreateTime).last("limit 1"));

                FileIpDO fileIpDO = fileIpMapper.selectOne(new QueryWrapper<FileIpDO>().lambda()
                    .eq(FileIpDO::getFileId, fileDO.getId()).orderByDesc(FileIpDO::getCreateTime).last("limit 1"));

                fileDTO.setLastFileLog(fileLogDO);
                fileDTO.setLastFileIp(fileIpDO);

                fileDTOList.add(fileDTO);
            }
            return new PageVO<>(page.getTotal(), fileDTOList);
        }

        if (CharSequenceUtil.isAllBlank(fileQuery.getFileName(), fileQuery.getFilePath())
            && CharSequenceUtil.isNotBlank(fileQuery.getLogContent())
            && CharSequenceUtil.isAllBlank(fileQuery.getIp(), fileQuery.getXLuminatiIp())) {
            Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
            List<
                FileLogDO> fileLogDOList =
                    fileLogMapper
                        .selectList(new QueryWrapper<FileLogDO>()
                            .lambda().like(CharSequenceUtil.isNotBlank(fileQuery.getLogContent()),
                                FileLogDO::getContent, fileQuery.getLogContent())
                            .orderByDesc(FileLogDO::getCreateTime));
            List<FileDTO> fileDTOList = new ArrayList<>();
            for (FileLogDO fileLogDO : fileLogDOList) {
                FileDTO fileDTO = new FileDTO();
                fileDTO.setLastFileLog(fileLogDO);

                FileDO fileDO = fileMapper.selectById(fileLogDO.getFileId());

                FileIpDO fileIpDO = fileIpMapper.selectOne(new QueryWrapper<FileIpDO>().lambda()
                    .eq(FileIpDO::getFileId, fileDO.getId()).orderByDesc(FileIpDO::getCreateTime).last("limit 1"));

                fileDTO.setFile(fileDO);
                fileDTO.setLastFileIp(fileIpDO);

                fileDTOList.add(fileDTO);

            }
            return new PageVO<>(page.getTotal(), fileDTOList);
        }

        if (CharSequenceUtil.isAllBlank(fileQuery.getFileName(), fileQuery.getFilePath())
            && CharSequenceUtil.isBlank(fileQuery.getLogContent()) && (CharSequenceUtil.isNotBlank(fileQuery.getIp())
                || CharSequenceUtil.isNotBlank(fileQuery.getXLuminatiIp()))) {
            Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());

            List<FileIpDO> fileIpDOList = fileIpMapper.selectList(new QueryWrapper<FileIpDO>().lambda()
                .eq(CharSequenceUtil.isNotBlank(fileQuery.getIp()), FileIpDO::getIp, fileQuery.getIp())
                .eq(CharSequenceUtil.isNotBlank(fileQuery.getXLuminatiIp()), FileIpDO::getXLuminatiIp,
                    fileQuery.getXLuminatiIp())
                .orderByDesc(FileIpDO::getCreateTime));

            List<FileDTO> fileDTOList = new ArrayList<>();
            for (FileIpDO fileIpDO : fileIpDOList) {
                FileDO fileDO = fileMapper.selectById(fileIpDO.getFileId());

                FileLogDO fileLogDO = fileLogMapper.selectOne(new QueryWrapper<FileLogDO>().lambda()
                    .eq(FileLogDO::getFileId, fileDO.getId()).orderByDesc(FileLogDO::getCreateTime).last("limit 1"));

                FileDTO fileDTO = new FileDTO();
                fileDTO.setFile(fileDO);
                fileDTO.setLastFileLog(fileLogDO);
                fileDTO.setLastFileIp(fileIpDO);
                fileDTOList.add(fileDTO);
            }
            return new PageVO<>(page.getTotal(), fileDTOList);
        }
        Page page = PageHelper.startPage(pageQuery.getPageNum(), pageQuery.getPageSize());
        List<FileDO> fileDOList = fileMapper.list(fileQuery.getFileName(), fileQuery.getFilePath(),
            fileQuery.getLogContent(), fileQuery.getXLuminatiIp(), fileQuery.getIp());
        List<FileDTO> fileDTOList = new ArrayList<>();
        for (FileDO fileDO : fileDOList) {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setFile(fileDO);

            FileLogDO fileLogDO = new FileLogDO();
            fileLogDO.setContent(fileDO.getLogContent());
            fileLogDO.setType(fileDO.getLogType());
            fileDTO.setLastFileLog(fileLogDO);

            FileIpDO fileIpDO = new FileIpDO();
            fileIpDO.setIp(fileDO.getIp());
            fileIpDO.setXLuminatiIp(fileDO.getXLuminatiIp());

            fileDTO.setLastFileIp(fileIpDO);
            fileDTOList.add(fileDTO);

        }
        return new PageVO<>(page.getTotal(), fileDTOList);
    }

    @Override
    public void saveFile(FileRequest fileRequest) {
        FileDO file =
            fileMapper.selectOne(new QueryWrapper<FileDO>().lambda().eq(FileDO::getName, fileRequest.getFileName()));
        // 如果文件名存在，添加或者修改文件路径，添加日志内容，添加xlumip、ip（如果都存在则不添加）
        // 如果文件名不存在，新增文件名、文件路径，再添加xlumip、ip、日志内容
        if (ObjectUtil.isNotNull(file)) {
            if (CharSequenceUtil.isNotBlank(fileRequest.getFilePath())) {
                file.setPath(fileRequest.getFilePath());
                fileMapper.updateById(file);
            }
            if (CharSequenceUtil.isNotBlank(fileRequest.getIp())
                || CharSequenceUtil.isNotBlank(fileRequest.getXLuminatiIp())) {
                long count =
                    fileIpMapper.selectCount(new QueryWrapper<FileIpDO>().lambda().eq(FileIpDO::getFileId, file.getId())
                        .eq(CharSequenceUtil.isNotBlank(fileRequest.getIp()), FileIpDO::getIp, fileRequest.getIp())
                        .eq(CharSequenceUtil.isNotBlank(fileRequest.getXLuminatiIp()), FileIpDO::getXLuminatiIp,
                            fileRequest.getXLuminatiIp()));
                if (count < 1) {
                    FileIpDO fileIpDO = new FileIpDO();
                    fileIpDO.setIp(fileRequest.getIp());
                    fileIpDO.setXLuminatiIp(fileRequest.getXLuminatiIp());
                    fileIpDO.setFileId(file.getId());
                    fileIpMapper.insert(fileIpDO);
                }
            }
        } else {
            file = new FileDO();
            file.setPath(fileRequest.getFilePath());
            file.setName(fileRequest.getFileName());
            fileMapper.insert(file);

            if (CharSequenceUtil.isNotBlank(fileRequest.getIp())
                || CharSequenceUtil.isNotBlank(fileRequest.getXLuminatiIp())) {
                FileIpDO fileIpDO = new FileIpDO();
                fileIpDO.setIp(fileRequest.getIp());
                fileIpDO.setXLuminatiIp(fileRequest.getXLuminatiIp());
                fileIpDO.setFileId(file.getId());
                fileIpMapper.insert(fileIpDO);
            }
        }

        if (CharSequenceUtil.isNotBlank(fileRequest.getLogContent())) {
            FileLogDO fileLogDO = new FileLogDO();
            fileLogDO.setContent(fileRequest.getLogContent());
            fileLogDO.setFileId(file.getId());
            fileLogDO.setType(1);
            fileLogMapper.insert(fileLogDO);
        }
    }

    @Override
    public FileDTO getFile(String fileName, Long fileId) {
        if (CharSequenceUtil.isBlank(fileName) && ObjectUtil.isNull(fileId)) {
            throw new BizException(ReturnCode.RC500.getCode(), "传参不能同时为空");
        }
        FileDO file = fileMapper.selectOne(
            new QueryWrapper<FileDO>().lambda().eq(CharSequenceUtil.isNotBlank(fileName), FileDO::getName, fileName)
                .eq(ObjectUtil.isNotNull(fileId), FileDO::getId, fileId));
        if (ObjectUtil.isNotNull(file)) {
            FileDTO fileDTO = new FileDTO();
            fileDTO.setFile(file);

            List<FileLogDO> fileLogDOList =
                fileLogMapper.selectList(new QueryWrapper<FileLogDO>().lambda().eq(FileLogDO::getFileId, file.getId()));
            fileDTO.setFileLogList(fileLogDOList);

            List<FileIpDO> fileIpDOList =
                fileIpMapper.selectList(new QueryWrapper<FileIpDO>().lambda().eq(FileIpDO::getFileId, file.getId()));
            fileDTO.setFileIpList(fileIpDOList);
            return fileDTO;
        }
        return null;
    }

    @Override
    public List<FileDTO> listFiles(List<Long> fileIds) {
        List<Long> newList = new ArrayList<>();
        for (Long fileId : fileIds) {
            if (!newList.contains(fileId)) {
                newList.add(fileId);
            }
        }
        List<FileDTO> fileDTOList = new ArrayList<>();
        for (Long fileId : newList) {
            FileDTO fileDTO = getFile(null, fileId);
            fileDTOList.add(fileDTO);
        }
        return fileDTOList;
    }
}
