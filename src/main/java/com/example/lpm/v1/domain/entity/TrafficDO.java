package com.example.lpm.v1.domain.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.example.lpm.v1.common.BaseEntity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema
@Data
@TableName("t_traffic")
public class TrafficDO extends BaseEntity {
    @Schema(description = "")
    private String serverAddr;

    @Schema(description = "")
    private String clientAddr;

    @Schema(description = "")
    private String targetAddr;

    @Schema(description = "")
    private String username;

    @Schema(description = "")
    private Long bytes;

    @Schema(description = "")
    private String outLocalAddr;

    @Schema(description = "")
    private String outRemoteAddr;

    @Schema(description = "")
    private String upstream;

    @Schema(description = "")
    private String serviceId;

}