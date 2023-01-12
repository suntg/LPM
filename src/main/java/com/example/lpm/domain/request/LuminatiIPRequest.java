package com.example.lpm.domain.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "LuminatiIP入参")
public class LuminatiIPRequest {

    @Schema(description = "socks端口")
    private String socksPort;

    @Schema(description = "socks用户名")
    private String socksUsername;

    @Schema(description = "socks密码")
    private String socksPassword;

    @Schema(description = "Luminati customer")
    private String customer;

    @Schema(description = "Luminati country")
    private String country;

    @Schema(description = "Luminati zone")
    private String zone;

    @Schema(description = "Luminati zonePassword")
    private String zonePassword;

    @Schema(description = "Luminati proxyUrl")
    private String proxyUrl;

    @Schema(description = "检查间隔时间，分钟")
    private int checkIPTime;

    private int socksPortCount;

    @Schema(description = "Luminati token x-luminati-ip")
    private String xLuminatiIp;
    @Schema(description = "Luminati ")
    private String ip;
}
