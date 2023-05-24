package com.example.lpm.v1.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.lpm.v1.domain.entity.FileDO;

@Mapper
public interface FileMapper extends BaseMapper<FileDO> {

    List<FileDO> list(@Param("fileName") String fileName, @Param("filePath") String filePath,
        @Param("logContent") String logContent, @Param("xLuminatiIp") String xLuminatiIp, @Param("ip") String ip);

}
