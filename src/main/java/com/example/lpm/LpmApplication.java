package com.example.lpm;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;

@ServletComponentScan(basePackages = "com.example.lpm")
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "LPM API", description = "LPM服务接口文档", version = "2.0.0"))
public class LpmApplication {

    public static void main(String[] args) {
        SpringApplication.run(LpmApplication.class, args);
    }

}
