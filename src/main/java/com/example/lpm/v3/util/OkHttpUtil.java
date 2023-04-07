package com.example.lpm.v3.util;

import cn.hutool.core.exceptions.ExceptionUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

@Slf4j
public class OkHttpUtil {


    public static long measureTotalBytes(Request request, Response response) {
        long bytes = 0L;
        try {
            bytes = bytes + request.headers().byteCount() + response.body().contentLength() + response.headers().byteCount();
            if (request.body() != null) {
                long requestContentLength = request.body().contentLength();
                bytes = bytes + requestContentLength;
            }
        } catch (IOException e) {
            log.error("request.body().contentLength() error:{}", ExceptionUtil.stacktraceToString(e));
        }

        return bytes;
    }
}
